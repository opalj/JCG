//#define _GNU_SOURCE

#include <jvmti.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <map>
#include <unordered_set>
#include <sys/socket.h>
#include <unistd.h>
#include <netinet/in.h>
#include <cstring>

using namespace std;

static jvmtiEnv *jvmti = NULL;

struct Callsite {
    int topLevel;
    jmethodID mid;
    jlocation loc;

    bool operator==(const Callsite &o) const {
        return topLevel == o.topLevel && mid == o.mid && loc == o.loc;
    }

    bool operator<(const Callsite &o) const {
        return topLevel < o.topLevel || topLevel == o.topLevel && (mid < o.mid || mid == o.mid && loc < o.loc);
    }
};

static map<Callsite, unordered_set<jmethodID>> cg;

static void getMethodNameSig(jmethodID mid, char** nameSig) {
    jclass cls;
    jvmti->GetMethodDeclaringClass(mid, &cls);

    char* cname;
    char* generic;
    jvmti->GetClassSignature(cls, &cname, &generic);

    jvmti->Deallocate((unsigned char*) generic);

    char* mname;
    char* sig;
    jvmti->GetMethodName(mid, &mname, &sig, &generic);

    asprintf(nameSig, "%s:%s%s", (cname == NULL ? "" : cname), (mname == NULL ? "" : mname), (sig == NULL ? "" : sig));

    jvmti->Deallocate((unsigned char*) cname);
    jvmti->Deallocate((unsigned char*) mname);
    jvmti->Deallocate((unsigned char*) sig);
    jvmti->Deallocate((unsigned char*) generic);
}

void JNICALL MethodEntry(jvmtiEnv *jvmti, JNIEnv* jni, jthread thread, jmethodID method) {
    jmethodID callee;
    jlocation loc;

    jvmti->GetFrameLocation(NULL, 0, &callee, &loc);

    jclass cls;
    jvmti->GetMethodDeclaringClass(callee, &cls);

    jmethodID caller;

    int err = jvmti->GetFrameLocation(NULL, 1, &caller, &loc);

    Callsite callsite;
    if(err==JVMTI_ERROR_NO_MORE_FRAMES) {
        callsite = Callsite{1, NULL, 0};
    } else {
        jvmti->GetMethodDeclaringClass(callee, &cls);
        callsite = Callsite{0, caller, loc};
    }

    auto& callees = cg.emplace(make_pair(move(callsite), unordered_set<jmethodID>())).first->second;
    callees.insert(callee);
}

void print_cg() {
    printf("CG Size: %d\n", cg.size());
    fflush(stdout);
    for (const auto& calls : cg){
        const Callsite callsite = calls.first;

        char* caller;
        jint lntEntries;
        jvmtiLineNumberEntry* lineNumbers = NULL;
        int lineNumber = -1;

        if(callsite.topLevel == 1){
            caller = (char*)"TopLevel";
        } else {
            getMethodNameSig(callsite.mid, &caller);
            int err = jvmti->GetLineNumberTable(callsite.mid, &lntEntries, &lineNumbers);
            if (err == JVMTI_ERROR_NONE) {
                lineNumber = lineNumbers[0].line_number;
                for (int i = 1; i < lntEntries; i++) {
                    if (callsite.loc < lineNumbers[i].start_location) {
                        break;
                    }
                    lineNumber = lineNumbers[i].line_number;
                }
            }
        }

        const unordered_set<jmethodID>& callees = calls.second;

        for (const jmethodID mid : callees){
            char* callee;
            getMethodNameSig(mid, &callee);
            printf("Call: %s:%d:%d⇒%s\n", caller, callsite.loc, lineNumber, callee);
            fflush(stdout);
            free(callee);
        }

        if(callsite.topLevel == 0){
            free(caller);
            jvmti->Deallocate((unsigned char*) lineNumbers);
        }
    }
}

JNIEXPORT void JNICALL VMDeath(jvmtiEnv *jvmti_env, JNIEnv* jni_env) {
    print_cg();
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved) {
    vm->GetEnv((void**)&jvmti, JVMTI_VERSION_1_0);

    int server_socket, channel;
    struct sockaddr_in address;
    int opt = 1;
    int addrlen = sizeof(address);
    char* msg = "Test";

    server_socket = socket(AF_INET, SOCK_STREAM, 0);

    setsockopt(server_socket, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt));
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(atoi(options));

    bind(server_socket, (struct sockaddr*)&address, sizeof(address));
    listen(server_socket, 3);
    channel = accept(server_socket, (struct sockaddr*)&address, (socklen_t*)&addrlen);
    send(channel, msg, strlen(msg), 0);

    close(channel);
    shutdown(server_socket, SHUT_RDWR);

    jvmtiCapabilities capabilities = {0};
    capabilities.can_generate_method_entry_events = 1;
    capabilities.can_get_line_numbers = 1;
    jvmti->AddCapabilities(&capabilities);

    jvmtiEventCallbacks callbacks = {0};
    callbacks.MethodEntry = MethodEntry;
    callbacks.VMDeath = VMDeath;
    jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));

    jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, NULL);
    jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH, NULL);

    return 0;
}
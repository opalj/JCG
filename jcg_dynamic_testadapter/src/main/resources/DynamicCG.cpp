//#define _GNU_SOURCE

#include <jvmti.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <map>
#include <unordered_set>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <unistd.h>
#include <cstring>
#include <sstream>

using namespace std;

static jvmtiEnv *jvmti = NULL;
static int port = 0;

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
    int err = jvmti->GetMethodDeclaringClass(mid, &cls);

    if(err == JVMTI_ERROR_NONE){
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
    } else {
        asprintf(nameSig, "<FAILED>");
    }

    
}

void JNICALL MethodEntry(jvmtiEnv *jvmti, JNIEnv* jni, jthread thread, jmethodID method) {
    jlocation loc;

    // Get the caller method and call location from the previous stack frame
    jmethodID caller;
    int err = jvmti->GetFrameLocation(NULL, 1, &caller, &loc);

    Callsite callsite;
    if(err==JVMTI_ERROR_NO_MORE_FRAMES) {
        callsite = Callsite{1, NULL, 0}; // Top-level call without caller
    } else {
        callsite = Callsite{0, caller, loc}; // Regular call site
    }

    // Add call site to the call graph if not already present, get the set of callees back
    auto& callees = cg.emplace(make_pair(move(callsite), unordered_set<jmethodID>())).first->second;
    // Insert callee into call site
    callees.insert(method);
}

void return_cg() {
    int channel;
    struct sockaddr_in serv_addr;
    char* msg = "Test";
    char* final_msg = "End of Callgraph";
    
    channel = socket(AF_INET, SOCK_STREAM, 0);

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(port);

    inet_pton(AF_INET, "127.0.0.1", &serv_addr.sin_addr);
    connect(channel, (struct sockaddr*)&serv_addr, sizeof(serv_addr));

    send(channel, msg, strlen(msg), 0);

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
            
            // Send caller, callsite.loc, lineNumber, callee
            stringstream ss_caller;
            stringstream ss_callsite;
            stringstream ss_lineNumber;
            stringstream ss_callee;
  	    
  	    ss_caller << caller << "\n";
  	    ss_callsite << callsite.loc<< "\n";
  	    ss_lineNumber << lineNumber<< "\n";
  	    ss_callee << callee<< "\n";
	  	
            send(channel, ss_caller.str().c_str(), ss_caller.str().length(), 0);
            send(channel, ss_callsite.str().c_str(), ss_callsite.str().length(), 0);
            send(channel, ss_lineNumber.str().c_str(), ss_lineNumber.str().length(), 0);
            send(channel, ss_callee.str().c_str(), ss_callee.str().length(), 0);
            
            free(callee);
        }

        if(callsite.topLevel == 0){
            free(caller);
            jvmti->Deallocate((unsigned char*) lineNumbers);
        }
    }

    send(channel, final_msg, strlen(final_msg), 0);
    close(channel);
}

JNIEXPORT void JNICALL VMDeath(jvmtiEnv *jvmti_env, JNIEnv* jni_env) {
    return_cg();
}

JNIEXPORT jint JNICALL Agent_OnLoad(JavaVM *vm, char *options, void *reserved) {
    port = atoi(options);

    vm->GetEnv((void**)&jvmti, JVMTI_VERSION_1_0);

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

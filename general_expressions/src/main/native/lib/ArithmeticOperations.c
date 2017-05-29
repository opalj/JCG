#include <lib.h>
#include <stdlib.h>
#include "ArithmeticOperations.h"

/*
 * Checks, weather the passed object is null.
 * If yes, a NullPointerException will be thrown with the passed message.
 */
int nullCheck(JNIEnv *env, jobject o, char* message) {
	jclass nullPointerException = (*env)->FindClass(env,
			"java/lang/NullPointerException");
	if (o == NULL) {
		//exception in transitively called method
		(*env)->ThrowNew(env, nullPointerException, message);
		return 0;
	}
	return 1;
}

jobject evaluateExpression(JNIEnv *env, jobject expression, jobject values) {
	jclass expressionType = (*env)->GetObjectClass(env, expression);
	jmethodID eval = (*env)->GetMethodID(env, expressionType, "eval",
			"(Lexpressions/Map;)Lexpressions/Constant;");
	//callback in transitively called method
	return (*env)->CallObjectMethod(env, expression, eval, values);
}

/*
 * Class:     expressions_jni_NativeAddExpression
 * Method:    eval
 * Signature: (Lexpressions/Map;)Lexpressions/Constant;
 */
JNIEXPORT jobject JNICALL Java_lib_NativeAddExpression_eval(
		JNIEnv *env, jobject this, jobject values) {
	/*
	 * Directly accesses the fields left and right on the passed expression.
	 * Calls eval(values) on the results in a transitively called method.
	 * Directly calls getValue() on the resulting constants.
	 * Throws an exception in a transitively called method, if a return value is null.
	 */
	jclass expressionClass = (*env)->GetObjectClass(env, this);
	jfieldID leftID = (*env)->GetFieldID(env, expressionClass, "left",
			"Ljava/lang/Object;");
	jfieldID rightID = (*env)->GetFieldID(env, expressionClass, "right",
			"Ljava/lang/Object;");
	jobject left = (*env)->GetObjectField(env, this, leftID);
	jobject right = (*env)->GetObjectField(env, this, rightID);
	if (nullCheck(env, left, "left()") == 0
			|| nullCheck(env, right, "right()") == 0) {
		return 0;
	}
	jobject leftConstant = evaluateExpression(env, left, values);
	jobject rightConstant = evaluateExpression(env, right, values);
	if (nullCheck(env, leftConstant, "left().eval(values)") == 0
			|| nullCheck(env, rightConstant, "right().eval(values)") == 0) {
		return 0;
	}
	jclass constant = (*env)->FindClass(env, "expressions/Constant");
	jmethodID getValue = (*env)->GetMethodID(env, constant, "getValue", "()I");
	jint leftValue = (*env)->CallObjectMethod(env, leftConstant, getValue);
	jint rightValue = (*env)->CallObjectMethod(env, rightConstant, getValue);
	jmethodID constantInit = (*env)->GetMethodID(env, constant, "<init>",
			"(I)V");
	jobject result = (*env)->NewObject(env, constant, constantInit,
			leftValue + rightValue);
	return result;
}

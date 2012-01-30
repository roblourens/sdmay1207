#include <jni.h>
#include <string.h>


JNIEXPORT jint JNICALL	Java_bachelor_TextMsg_TextMsg_runCommand (JNIEnv * env, jclass class, jstring command)
{
	const char * commandString;
	commandString =(*env)->GetStringUTFChars(env, command, 0);
	int exitcode =system(commandString);
	(*env)->ReleaseStringUTFChars(env, command, commandString);
	return (jint) exitcode;
}

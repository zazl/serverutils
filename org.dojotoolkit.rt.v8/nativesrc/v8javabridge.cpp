// v8javabridge.cpp : Defines the exported functions for the DLL application.
//
#define V8_TARGET_ARCH_IA32 1
#include <v8.h>
#include <iostream>
#include <string>
using namespace std;
#include "v8javabridge.h"

v8::Handle<v8::Value> Print(const v8::Arguments& args);
v8::Handle<v8::Value> ReadText(const v8::Arguments& args);
v8::Handle<v8::Value> Load(const v8::Arguments& args);
v8::Handle<v8::Value> LoadCommonJSModule(const v8::Arguments& args);
v8::Handle<v8::Value> CallbackHandler(const v8::Arguments& args);
v8::Handle<v8::Value> SetInterval(const v8::Arguments& args);
v8::Handle<v8::Value> ClearInterval(const v8::Arguments& args);

v8::Handle<v8::ObjectTemplate> CreateGlobal();
const char * readText(JNIEnv * env, jobject jobj, const char * url);
void ReportException(v8::TryCatch* handler, JNIEnv * env);
void ReportCompileError(v8::TryCatch* handler, JNIEnv * env, jobject jobj);
JNIEXPORT jstring JNICALL Java_org_dojotoolkit_rt_v8_V8JavaBridge_runScriptInV8(JNIEnv * env, jobject jobj, jstring jsource);
JNIEXPORT jstring JNICALL Java_org_dojotoolkit_rt_v8_V8JavaBridge_runScriptInV8WithCallbacks(JNIEnv * env, jobject jobj, jstring jsource, jobjectArray jcallbacks);
JNIEXPORT jstring JNICALL Java_org_dojotoolkit_rt_v8_V8JavaBridge_runScriptInV8WithExternalCallbacks(JNIEnv * env, jobject jobj, jstring jsource, jobjectArray jcallbacks, jobject jexternal);

JNIEXPORT jstring JNICALL Java_org_dojotoolkit_rt_v8_V8JavaBridge_runScriptInV8(JNIEnv * env, jobject jobj, jstring jsource) {
	return Java_org_dojotoolkit_rt_v8_V8JavaBridge_runScriptInV8WithCallbacks(env, jobj, jsource, NULL);
}

JNIEXPORT jstring JNICALL Java_org_dojotoolkit_rt_v8_V8JavaBridge_runScriptInV8WithCallbacks(JNIEnv * env, jobject jobj, jstring jsource, jobjectArray jcallbacks) {
	return Java_org_dojotoolkit_rt_v8_V8JavaBridge_runScriptInV8WithExternalCallbacks(env, jobj, jsource, jcallbacks, NULL);
}

JNIEXPORT jstring JNICALL Java_org_dojotoolkit_rt_v8_V8JavaBridge_runScriptInV8WithExternalCallbacks(JNIEnv * env, jobject jobj, jstring jsource, jobjectArray jcallbacks, jobject jexternal) {
	v8::Locker locker;
	v8::HandleScope handle_scope;
	v8::Handle<v8::ObjectTemplate> global = v8::ObjectTemplate::New();

	global->Set(v8::String::New("print"), v8::FunctionTemplate::New(Print));
	global->Set(v8::String::New("readText"), v8::FunctionTemplate::New(ReadText));
	global->Set(v8::String::New("loadJS"), v8::FunctionTemplate::New(Load));
	global->Set(v8::String::New("loadCommonJSModule"), v8::FunctionTemplate::New(LoadCommonJSModule));
	global->Set(v8::String::New("setInterval"), v8::FunctionTemplate::New(SetInterval));
	global->Set(v8::String::New("clearInterval"), v8::FunctionTemplate::New(ClearInterval));

	jboolean iscopy;
	if (jcallbacks != NULL) {
		for (int i = 0; i < env->GetArrayLength(jcallbacks); i++) {
			v8::HandleScope handle_scope;
			jstring jcallback = (jstring)env->GetObjectArrayElement(jcallbacks, i);
			v8::Handle<v8::String> callback = v8::String::New(env->GetStringUTFChars(jcallback, &iscopy));
			global->Set(callback, v8::FunctionTemplate::New(CallbackHandler));
		}
	}
	global->Set(v8::String::New("jniEnv"), v8::External::New(env));
	global->Set(v8::String::New("jobject"), v8::External::New(jobj));

	if (jexternal != NULL) {
		global->Set(v8::String::New("jexternal"), v8::External::New(jexternal));
	}

	v8::Handle<v8::Context> context = v8::Context::New(NULL, global);
	v8::Context::Scope context_scope(context);

	v8::TryCatch try_catch;

	const char *source = env->GetStringUTFChars(jsource, &iscopy);

    v8::Handle<v8::String> v8source = v8::String::New(source);
    v8::Handle<v8::String> v8name = v8::String::New("script");

	v8::Handle<v8::Script> script = v8::Script::Compile(v8source, v8name);

	jstring returnValue = NULL;

	if (script.IsEmpty()) {
		ReportException(&try_catch, env);
	} else {
		v8::Handle<v8::Value> result = script->Run();
		if (!result.IsEmpty() && !result->IsUndefined()) {
			v8::String::Utf8Value resultStr(result);
			returnValue = env->NewStringUTF(*resultStr);
		}
		else if (try_catch.HasCaught()) {
			ReportException(&try_catch, env);
		}
	}
	return returnValue;
}

v8::Handle<v8::Value> Print(const v8::Arguments& args) {
    v8::HandleScope handle_scope;

	v8::Handle<v8::Context> context = v8::Context::GetCalling();

	v8::Local<v8::Value> envValue = context->Global()->Get(v8::String::New("jniEnv"));
	v8::Local<v8::External> extEnv = v8::External::Cast(*envValue);
	JNIEnv * env = (JNIEnv *) extEnv->Value();

	v8::Local<v8::Value> jobjectValue = context->Global()->Get(v8::String::New("jobject"));
	v8::Local<v8::External> extjobject = v8::External::Cast(*jobjectValue);
	jobject jobj = (jobject) extjobject->Value();

	jobjectArray msgs = (jobjectArray)env->NewObjectArray(args.Length(), env->FindClass("java/lang/String"), env->NewStringUTF(""));

	for (int i = 0; i < args.Length(); i++) {
		v8::HandleScope handle_scope;
		v8::String::Utf8Value str(args[i]);
		const char* cstr = *str;
		env->SetObjectArrayElement(msgs, i, env->NewStringUTF(cstr));
	}

	jclass cls = env->GetObjectClass(jobj);
	
	jmethodID mid = env->GetMethodID(cls, "print", "([Ljava/lang/String;)V");
	if (mid == 0) {
		cout << "Can't find method print" << endl;
		return v8::Undefined();
	}

	env->ExceptionClear();
	env->CallVoidMethod(jobj, mid, msgs);

	if(env->ExceptionOccurred()) {
		cout << "hit error :(" << endl;
		env->ExceptionDescribe();
		env->ExceptionClear();
	}

	return v8::Undefined();
}

v8::Handle<v8::Value> ReadText(const v8::Arguments& args) {
	v8::HandleScope handle_scope;
	v8::Handle<v8::Context> context = v8::Context::GetCalling();

	v8::Local<v8::Value> envValue = context->Global()->Get(v8::String::New("jniEnv"));
	v8::Local<v8::External> extEnv = v8::External::Cast(*envValue);
	JNIEnv * env = (JNIEnv *) extEnv->Value();

	v8::Local<v8::Value> jobjectValue = context->Global()->Get(v8::String::New("jobject"));
	v8::Local<v8::External> extjobject = v8::External::Cast(*jobjectValue);
	jobject jobj = (jobject) extjobject->Value();

	v8::String::Utf8Value str(args[0]);

	const char * text = readText(env, jobj, *str);
	if (text == NULL) {
		return v8::Null();
	}
	else {
		return v8::String::New(text);
	}
}

v8::Handle<v8::Value> Load(const v8::Arguments& args) {
    v8::HandleScope handle_scope;

	v8::Handle<v8::Context> context = v8::Context::GetCalling();

	v8::Local<v8::Value> envValue = context->Global()->Get(v8::String::New("jniEnv"));
	v8::Local<v8::External> extEnv = v8::External::Cast(*envValue);
	JNIEnv * env = (JNIEnv *) extEnv->Value();

	v8::Local<v8::Value> jobjectValue = context->Global()->Get(v8::String::New("jobject"));
	v8::Local<v8::External> extjobject = v8::External::Cast(*jobjectValue);
	jobject jobj = (jobject) extjobject->Value();

	v8::String::Utf8Value str(args[0]);

	const char* url = *str;

	const char *textRead = readText(env, jobj, url);
	if (textRead != NULL) {
		v8::Handle<v8::String> text = v8::String::New(textRead);
		v8::TryCatch try_catch;
		v8::Handle<v8::Script> script = v8::Script::Compile(text, args[0]->ToString());

		if (script.IsEmpty()) {
			ReportCompileError(&try_catch, env, jobj);
			return v8::Null();
		} else {
			v8::Handle<v8::Value> result = script->Run();
			return result;
		}
	}
	else {
		return v8::Null();
	}
}

v8::Handle<v8::Value> LoadCommonJSModule(const v8::Arguments& args) {
    v8::HandleScope handle_scope;
	
	v8::Handle<v8::Context> context = v8::Context::GetCalling();
	
	v8::Local<v8::Value> envValue = context->Global()->Get(v8::String::New("jniEnv"));
	v8::Local<v8::External> extEnv = v8::External::Cast(*envValue);
	JNIEnv * env = (JNIEnv *) extEnv->Value();
	
	v8::Local<v8::Value> jobjectValue = context->Global()->Get(v8::String::New("jobject"));
	v8::Local<v8::External> extjobject = v8::External::Cast(*jobjectValue);
	jobject jobj = (jobject) extjobject->Value();
	
	v8::String::Utf8Value str(args[0]);
	
	const char* url = *str;
	
	const char *textRead = readText(env, jobj, url);
	if (textRead != NULL) {
		v8::Handle<v8::Value> result = v8::Null();
		v8::Handle<v8::ObjectTemplate> global = CreateGlobal();
		v8::Handle<v8::Context> moduleContext = v8::Context::New(NULL, global);
		v8::Handle<v8::Value> requireValue = context->Global()->Get(v8::String::New("require"));
		v8::Context::Scope context_scope(moduleContext);
		//moduleContext->Enter();
		moduleContext->Global()->Set(v8::String::New("require"), requireValue);
		moduleContext->Global()->Set(v8::String::New("jniEnv"), v8::External::New(env));
		moduleContext->Global()->Set(v8::String::New("jobject"), v8::External::New(jobj));

		v8::Handle<v8::String> text = v8::String::New(textRead);
		v8::TryCatch try_catch;
		v8::Handle<v8::Script> script = v8::Script::New(text, args[0]->ToString());
		
		if (script.IsEmpty()) {
			ReportCompileError(&try_catch, env, jobj);
		} else {
			v8::Local<v8::Object> module = args[1]->ToObject();
			v8::Local<v8::Array> keys = module->GetPropertyNames();
			
			unsigned int i;
			for (i = 0; i < keys->Length(); i++) {
				v8::Handle<v8::String> key = keys->Get(v8::Integer::New(i))->ToString();
				v8::String::Utf8Value keystr(key);
				v8::Handle<v8::Value> value = module->Get(key);
				moduleContext->Global()->Set(key, value);
			}

			result = script->Run();
			if (try_catch.HasCaught()) {
				ReportException(&try_catch, env);
			}
		}
		//moduleContext->DetachGlobal();
		//moduleContext->Exit();
		return result;
	}
	else {
		return v8::Null();
	}
}

v8::Handle<v8::Value> CallbackHandler(const v8::Arguments& args) {
    v8::HandleScope handle_scope;

	v8::Handle<v8::Context> context = v8::Context::GetCalling();

	v8::Local<v8::Value> envValue = context->Global()->Get(v8::String::New("jniEnv"));
	v8::Local<v8::External> extEnv = v8::External::Cast(*envValue);
	JNIEnv * env = (JNIEnv *) extEnv->Value();

	v8::Local<v8::Value> jobjectValue = context->Global()->Get(v8::String::New("jobject"));
	v8::Local<v8::External> extjobject = v8::External::Cast(*jobjectValue);
	jobject jobj = (jobject) extjobject->Value();

	jobject jexternal = NULL;

	v8::Local<v8::Value> jexternalValue = context->Global()->Get(v8::String::New("jexternal"));

	if (jexternalValue != v8::Undefined()) {
		v8::Local<v8::External> extjexternal = v8::External::Cast(*jexternalValue);
		jexternal = (jobject) extjexternal->Value();
	}

	v8::String::Utf8Value callbackName(args.Callee()->GetName());
	jmethodID mid = 0;
	jobject callbackObject = NULL;

	if (jexternal != NULL) {
		mid = env->GetMethodID(env->GetObjectClass(jexternal), *callbackName, "(Ljava/lang/String;)Ljava/lang/String;");
		callbackObject = jexternal;
	}

	if (mid == 0) {
		env->ExceptionClear();
		mid = env->GetMethodID(env->GetObjectClass(jobj), *callbackName, "(Ljava/lang/String;)Ljava/lang/String;");
		callbackObject = jobj;
	}

	if (mid == 0) {
		cout << "Can't find method " << *callbackName << endl;
		return v8::Undefined();
	}

	jclass throwableClass = env->FindClass("java/lang/Throwable");
	jmethodID getMessageMID = env->GetMethodID(throwableClass, "getMessage", "()Ljava/lang/String;");

	env->ExceptionClear();
	v8::String::Utf8Value param(args[0]);
	jstring jparam = env->NewStringUTF(*param);
	jstring jresult = NULL;
	{
		v8::Unlocker unlocker;
		jresult = (jstring)env->CallObjectMethod(callbackObject, mid, jparam);
	}
	jboolean iscopy;
	jthrowable exception = env->ExceptionOccurred();
	if (exception) {
		jstring jexcMessage = (jstring)env->CallObjectMethod(exception, getMessageMID);
		const char *excMessage = env->GetStringUTFChars(jexcMessage, &iscopy);
		string exceptionJSON("{_exceptionThrown : '");
		exceptionJSON += excMessage;
		exceptionJSON += "'}";
		env->ExceptionClear();
		return v8::String::New(exceptionJSON.c_str());
	}
	else {
		const char *result = env->GetStringUTFChars(jresult, &iscopy);
		return v8::String::New(result);
	}
}

const char * readText(JNIEnv * env, jobject jobj, const char * url) {
	jstring jurl = env->NewStringUTF(url);
	
	jclass cls = env->GetObjectClass(jobj);
	
	jmethodID mid = env->GetMethodID(cls, "readText", "(Ljava/lang/String;)Ljava/lang/String;");
	if (mid == 0) {
		cout << "Can't find method readText" << endl;
		return NULL;
	}

	env->ExceptionClear();

	jstring jtext = (jstring)env->CallObjectMethod(jobj, mid, jurl);
	if (jtext == NULL) {
		return NULL;
	}

	jboolean iscopy;

	const char * text = env->GetStringUTFChars(jtext, &iscopy);
	return text;
}

void ReportException(v8::TryCatch* try_catch, JNIEnv * env) {
	v8::HandleScope handle_scope;
	v8::String::Utf8Value exception(try_catch->Exception());
	v8::Handle<v8::Message> message = try_catch->Message();

	jclass excCls = env->FindClass("org/dojotoolkit/rt/v8/V8Exception");

	if (message.IsEmpty()) {
		env->ThrowNew(excCls, *exception); 
	} else {
		jmethodID cid = env->GetMethodID(excCls, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;III)V");
		if (cid == 0) {
			cout << "Can't find constructor for V8Exception" << endl;
			return;
		}
		jstring jexception_string = env->NewStringUTF(*exception);

		v8::String::Utf8Value filename(message->GetScriptResourceName());
		jstring jfilename = env->NewStringUTF(*filename);

		int linenum = message->GetLineNumber();
		jint jlinenum = (jint)linenum;

		v8::String::Utf8Value sourceline(message->GetSourceLine());
		jstring jsourceline = env->NewStringUTF(*sourceline);

		int start = message->GetStartColumn();
		jint jstart = (jint)start;
		int end = message->GetEndColumn();
		jint jend = (jint)end;

		jobject exc = env->NewObject(excCls, cid, jexception_string, jfilename, jsourceline, jlinenum, jstart, jend);
		env->Throw((jthrowable)exc);
	}
}

void ReportCompileError(v8::TryCatch* try_catch, JNIEnv * env, jobject jobj) {
	jclass cls = env->GetObjectClass(jobj);
	jmethodID mid = env->GetMethodID(cls, "reportCompileError", "(Ljava/lang/Throwable;)V");
	if (mid == 0) {
		cout << "Can't find method reportCompileError" << endl;
		return;
	}

	v8::HandleScope handle_scope;
	v8::String::Utf8Value exception(try_catch->Exception());
	v8::Handle<v8::Message> message = try_catch->Message();

	jclass excCls = env->FindClass("org/dojotoolkit/rt/v8/V8Exception");

	if (message.IsEmpty()) {
		jmethodID cid = env->GetMethodID(excCls, "<init>", "(Ljava/lang/String;)V");
		if (cid == 0) {
			cout << "Can't find constructor for V8Exception" << endl;
			return;
		}
		jstring jexception_string = env->NewStringUTF(*exception);
		jobject exc = env->NewObject(excCls, cid, jexception_string);
		env->ExceptionClear();
		env->CallVoidMethod(jobj, mid, (jthrowable)exc);
	} else {
		jmethodID cid = env->GetMethodID(excCls, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;III)V");
		if (cid == 0) {
			cout << "Can't find constructor for V8Exception" << endl;
			return;
		}
		jstring jexception_string = env->NewStringUTF(*exception);

		v8::String::Utf8Value filename(message->GetScriptResourceName());
		jstring jfilename = env->NewStringUTF(*filename);

		int linenum = message->GetLineNumber();
		jint jlinenum = (jint)linenum;

		v8::String::Utf8Value sourceline(message->GetSourceLine());
		jstring jsourceline = env->NewStringUTF(*sourceline);

		int start = message->GetStartColumn();
		jint jstart = (jint)start;
		int end = message->GetEndColumn();
		jint jend = (jint)end;

		jobject exc = env->NewObject(excCls, cid, jexception_string, jfilename, jsourceline, jlinenum, jstart, jend);
		env->ExceptionClear();
		env->CallVoidMethod(jobj, mid, (jthrowable)exc);
	}
}

v8::Handle<v8::Value> SetInterval(const v8::Arguments& args) {
    v8::HandleScope handle_scope;
	v8::Handle<v8::Context> context = v8::Context::GetCalling();

	v8::Local<v8::Function> fn = v8::Function::Cast(*args[0]);
	fn->Call(context->Global(), 0, NULL);
	return v8::Undefined();
}

v8::Handle<v8::Value> ClearInterval(const v8::Arguments& args) {
    v8::HandleScope handle_scope;
	return v8::Undefined();
}

v8::Handle<v8::ObjectTemplate> CreateGlobal() {
	v8::Handle<v8::ObjectTemplate> global = v8::ObjectTemplate::New();
	
	global->Set(v8::String::New("print"), v8::FunctionTemplate::New(Print));
	global->Set(v8::String::New("readText"), v8::FunctionTemplate::New(ReadText));
	global->Set(v8::String::New("loadJS"), v8::FunctionTemplate::New(Load));
	global->Set(v8::String::New("loadCommonJSModule"), v8::FunctionTemplate::New(LoadCommonJSModule));
	global->Set(v8::String::New("setInterval"), v8::FunctionTemplate::New(SetInterval));
	global->Set(v8::String::New("clearInterval"), v8::FunctionTemplate::New(ClearInterval));
	return global;
}

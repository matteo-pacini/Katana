#include "com_matteopacini_katana_KatanaAsyncTask.h"
#include "global.h"

JNIEXPORT jint JNICALL Java_com_matteopacini_katana_KatanaAsyncTask_split
(JNIEnv *env, jclass class, jstring inputFile, jstring inputFileBasename, jstring outputDirectory, jlong fileSize, jlong splitSize, jboolean preserve)
{
	char buffer[BUFFER_SIZE];
	unsigned long long splitPos, splitMustRead, bytesToGo;

	const char *_inputFile = (*env)->GetStringUTFChars(env, inputFile, NULL);
	const char *_inputFileBasename = (*env)->GetStringUTFChars(env, inputFileBasename, NULL);
	const char *_outputDirectory = (*env)->GetStringUTFChars(env, outputDirectory, NULL);
	unsigned long long _fileSize = (unsigned long long)fileSize;
	unsigned long long _splitSize = (unsigned long long)splitSize;

	char *splitFile = (char*)malloc(sizeof(char)*(strlen(_outputDirectory) + strlen(_inputFileBasename)+6));

	FILE *cFile = fopen(_inputFile,"rb");
	FILE *cSplitFile = NULL;

	unsigned int piece, pieces = (unsigned int)(_fileSize%_splitSize?(_fileSize/_splitSize)+1:_fileSize/_splitSize);

	for (piece = 1; piece <= pieces; piece++)
	{
		splitPos = 0;
		splitMustRead = (piece==pieces?fileSize-((pieces-1)*(splitSize)):splitSize);

		sprintf(splitFile,"%s/%s.%03i",_outputDirectory,_inputFileBasename,piece);

		cSplitFile = fopen(splitFile,"wb");

		while (splitPos < splitMustRead)
		{

			bytesToGo = splitMustRead - splitPos;

		    if (bytesToGo >= BUFFER_SIZE)
		    {
		    	fread(buffer, 1, BUFFER_SIZE, cFile);
		        splitPos += fwrite(buffer, 1, BUFFER_SIZE, cSplitFile);

		    } else
		    {
		        fread(buffer, 1, bytesToGo, cFile);
		        splitPos += fwrite(buffer, 1, bytesToGo, cSplitFile);
		    }

		}

		fclose(cSplitFile);
		cSplitFile = NULL;

	}

	fclose(cFile);

	if (!preserve)
	{
		unlink(_inputFile);
	}

	(*env)->ReleaseStringUTFChars(env, inputFile, _inputFile);
	(*env)->ReleaseStringUTFChars(env, inputFileBasename, _inputFileBasename);
	(*env)->ReleaseStringUTFChars(env, outputDirectory, _outputDirectory);

	free(splitFile);

	return 0;
}


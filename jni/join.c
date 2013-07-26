////////////////////////////////////////////////////////////////////////////
//                                                                        //
//  This file is part of Katana.                                          //
//                                                                        //
//  Katana is free software: you can redistribute it and/or modify        //
//  it under the terms of the GNU General Public License as published by  //
//  the Free Software Foundation, either version 3 of the License, or     //
//  any later version.                                                    //
//                                                                        //
//  Katana is distributed in the hope that it will be useful,             //
//  but WITHOUT ANY WARRANTY; without even the implied warranty of        //
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         //
//  GNU General Public License for more details.                          //
//                                                                        //
//  You should have received a copy of the GNU General Public License     //
//  along with Katana. If not, see <http://www.gnu.org/licenses/>.        //
//                                                                        //
////////////////////////////////////////////////////////////////////////////

#include "com_matteopacini_katana_KatanaAsyncTask.h"
#include "global.h"

#include <unistd.h>

JNIEXPORT jint JNICALL Java_com_matteopacini_katana_KatanaAsyncTask_join
  (JNIEnv *env, jclass class, jobjectArray inputFiles, jstring originalFile)
{
	char buffer[BUFFER_SIZE];
	unsigned long long splitPos, splitMustRead, bytesToGo;

	const char *_originalFile = (*env)->GetStringUTFChars(env, originalFile, NULL);
	jsize totalPieces = (*env)->GetArrayLength(env, inputFiles);

	FILE *file = fopen(_originalFile,"wb");
	int index;

	for(index=0;index<totalPieces;index++)
	{

		jstring actualPiece = (jstring)(*env)->GetObjectArrayElement(env,inputFiles,index);
		const char *_actualPiece = (*env)->GetStringUTFChars(env, actualPiece, NULL);

		FILE *actualPieceFile = fopen(_actualPiece,"rb");

		splitPos = 0;

		//Dimensioni del file da leggere
		fseek(actualPieceFile,0L,SEEK_END);
		splitMustRead = ftell(actualPieceFile);
		fseek(actualPieceFile, 0L, SEEK_SET);

		while (splitPos < splitMustRead)
		{
			bytesToGo = splitMustRead - splitPos;

			if (bytesToGo >= BUFFER_SIZE)
			{
		    	fread(buffer, 1, BUFFER_SIZE, actualPieceFile);
		    	splitPos += fwrite(buffer, 1, BUFFER_SIZE, file);

			} else
			{
		        fread(buffer, 1, bytesToGo, actualPieceFile);
		        splitPos += fwrite(buffer, 1, bytesToGo, file);
			}
		}

		fclose(actualPieceFile);

		unlink(_actualPiece);

		(*env)->ReleaseStringUTFChars(env, actualPiece, _actualPiece);
		(*env)->DeleteLocalRef(env,actualPiece);

	}

	fclose(file);

	(*env)->ReleaseStringUTFChars(env, originalFile, _originalFile);

	return 0;
}

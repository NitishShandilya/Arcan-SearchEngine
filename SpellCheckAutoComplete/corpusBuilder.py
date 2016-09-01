# -*- coding: utf-8 -*-
import html2text
import os
import sys
reload(sys)
sys.setdefaultencoding('utf-8')

pathOfRoot = 'C:\downloaded_files_encoded'



count = 0
for rootPath, subdirs, allFiles in os.walk(pathOfRoot):
    for everyFile in allFiles:
        print everyFile
        if '2Fdevelopment_new%2Fpdfs%2F' in everyFile:
            continue
        count +=1
        eachFle = os.path.join(rootPath, everyFile)
        with open(eachFle, 'r') as openedFile:
            dataInFile = openedFile.read()
            parsedData = html2text.html2text(dataInFile)
            parsedData.replace('\n','').replace('\t','')
            w = open("out5.txt", "a+")
            w.write(parsedData.decode('utf-8'))
            openedFile.close()
            w.close()

print count
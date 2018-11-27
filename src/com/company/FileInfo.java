package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

public class FileInfo {

        public String _name;
        public String _path;
        public String _extention;
        public long _size;
        public FileTime _lastModifiedTime;

        public FileInfo(String fileName)
        {
            try {
                this._path = Paths.get(fileName).getParent().toString();
                this._name = Paths.get(fileName).getFileName().toString();
                this._size = Files.size(Paths.get(fileName));
                this._lastModifiedTime = Files.getLastModifiedTime(Paths.get(fileName));
                this._extention = this._name.substring(this._name.lastIndexOf(".") + 1);
            }
            catch (IOException IOEx)
            {
                System.out.println("IO Error reading file " + fileName);
                System.out.println(IOEx.getMessage());

            }
        }
}

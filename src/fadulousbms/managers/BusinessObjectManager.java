package fadulousbms.managers;

import fadulousbms.auxilary.IO;

import java.io.*;

/**
 * Created by ghost on 2017/01/11.
 */
public abstract class BusinessObjectManager
{
    public abstract void initialize();

    public boolean isSerialized(String path)
    {
        File file = new File(path);
        return file.exists();
    }

    public void serialize(String file_path, Object obj) throws IOException
    {
        String path = file_path.substring(0, file_path.lastIndexOf("/")+1);
        File fpath = new File(path);
        if(!fpath.exists())
        {
            if (fpath.mkdirs())
            {
                IO.log(getClass().getName(), IO.TAG_INFO,"successfully created new directory["+path+"].");
            } else {
                IO.log(getClass().getName(), IO.TAG_ERROR,"could not create new directory["+path+"].");
                return;
            }
        }else IO.log(getClass().getName(), IO.TAG_INFO, "directory["+path+"] already exists.");

        File file = new File(file_path);
        if(!file.exists())
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
            out.writeObject(obj);
            out.flush();
            out.close();
            IO.log(getClass().getName(), IO.TAG_INFO, "successfully serialized file["+file.getAbsolutePath()+"] to disk.");
        }else IO.log(getClass().getName(), IO.TAG_INFO, "file["+file.getAbsolutePath()+"] is already up-to-date.");
    }

    public Object deserialize(String path) throws IOException, ClassNotFoundException
    {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(path)));
        Object obj = in.readObject();
        in.close();
        IO.log(getClass().getName(), IO.TAG_INFO, "successfully deserialized file ["+path+"].");
        return obj;
    }
}

package archimulator.util.io.serialization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class GZIPObjectPersister<T> {
    public void serialize(T obj, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            GZIPOutputStream gzipos = new GZIPOutputStream(fos);

            this.write(obj, gzipos);

            gzipos.finish();
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public T deserialize(Class<? extends T> clz, String fileName) {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            GZIPInputStream gzipis = new GZIPInputStream(fis);

            T obj = this.read(clz, gzipis);

            fis.close();
            return obj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void write(T obj, GZIPOutputStream gzipos);

    protected abstract T read(Class<? extends T> clz, GZIPInputStream gzipis);
}

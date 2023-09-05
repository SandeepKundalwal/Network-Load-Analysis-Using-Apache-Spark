package spark.streaming.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class IPPacket {
    public Long time;
    public Integer totalSize;
    public Set<String> ipSet;
    public IPPacket() {
        ipSet = new HashSet<>();
    }

    @Override
    public String toString() {
        return "IPPacket{" +
                "time=" + time +
                ", totalSize=" + totalSize +
                ", ipSet=" + ipSet +
                '}';
    }
}

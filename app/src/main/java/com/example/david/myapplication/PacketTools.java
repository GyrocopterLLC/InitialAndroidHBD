package com.example.david.myapplication;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.CRC32;

public class PacketTools {
    private final byte SOP1 = (byte)0x9A;
    private final byte SOP2 = (byte)0xCC;

    private CRC32 crc_generator = null;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public StringBuffer create_crc32_packed(StringBuffer protectMe) {

        StringBuffer retStringB = new StringBuffer(4);

        if(crc_generator == null) {
            crc_generator = new CRC32();
        }
        crc_generator.reset();

        if(protectMe.length() % 4 != 0) {
            int addZeros = 4 - (protectMe.length() % 4);
            for (int i = 0; i < addZeros; i++) {
                protectMe.append((char) 0);
            }
        }
        crc_generator.update(protectMe.toString().getBytes(StandardCharsets.UTF_8));

        long crc32_l = crc_generator.getValue();
        retStringB.append( (char)((crc32_l & 0xFF000000L)>>24));
        retStringB.append( (char)((crc32_l & 0x00FF0000L)>>16));
        retStringB.append( (char)((crc32_l & 0x0000FF00L)>>8));
        retStringB.append( (char)((crc32_l & 0x000000FFL)>>0));
        return retStringB;
    }

    /**
     * Creates packets using the ebike-controller format.
     * Packets are constructed as follows:
     *
     *     0->1: Start of Packet
     *        2: PacketID
     *        3: nPacketID
     *     4->5: length of data field (n)
     *   6->6+n: data
     * 7+n-10+n: CRC32 on bytes 0->6+n
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public StringBuffer Pack(byte packetID, byte[] data) {
        StringBuffer retStringB = new StringBuffer(data.length+10);

        // add SOP bytes
        retStringB.append((char)SOP1);
        retStringB.append((char)SOP2);
        // add packet id
        byte nPacketID = (byte)((~packetID)&0xFF);
        retStringB.append((char)packetID);
        retStringB.append((char)nPacketID);
        byte len1 = (byte)((data.length & 0xFF00) >> 8);
        byte len2 = (byte)(data.length & 0x00FF);
        // add data length
        retStringB.append((char)len1);
        retStringB.append((char)len2);
        // add data

        for(int i = 0; i < data.length; i++) {
            retStringB.append((char)data[i]);
        }
        // add crc32
        retStringB.append(create_crc32_packed(retStringB));

        return retStringB;
    }

}

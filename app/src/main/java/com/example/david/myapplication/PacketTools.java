package com.example.david.myapplication;

import android.support.v4.content.res.TypedArrayUtils;

import java.util.ArrayList;
import java.util.zip.CRC32;

public class PacketTools {
    byte SOP1 = (byte)0x9A;
    byte SOP2 = (byte)0xCC;

    public Byte[] create_crc32_packed(Byte[] protectMe) {
        CRC32 crc_generator = new CRC32();
        crc_generator.reset();
        Byte[] crc32 = new Byte[4];

        if(protectMe.length % 4 != 0) {
            int addZeros = 4 - (protectMe.length % 4);
            Byte[] protectMeMore = new Byte[protectMe.length + addZeros];
            System.arraycopy(protectMe,0,protectMeMore,0,protectMe.length);
            for(int i = 0; i < addZeros; i++) {
                protectMeMore[protectMe.length + i] = (byte)0;
            }
            for(int i = 0; i < protectMeMore.length; i++) {
                crc_generator.update(protectMeMore[i].byteValue());
            }
        } else {
            for(int i = 0; i < protectMe.length; i++) {
                crc_generator.update(protectMe[i].byteValue());
            }
        }
        long crc32_l = crc_generator.getValue();
        crc32[0] = (byte)((crc32_l & 0xFF000000L)>>24);
        crc32[1] = (byte)((crc32_l & 0x00FF0000L)>>16);
        crc32[2] = (byte)((crc32_l & 0x0000FF00L)>>8);
        crc32[3] = (byte)((crc32_l & 0x000000FFL)>>0);
        return crc32;
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
    public byte[] Pack(byte packetID, byte[] data) {
        ArrayList<Byte> tempArray = new ArrayList<Byte>();

        // add SOP bytes
        tempArray.add(SOP1);
        tempArray.add(SOP2);
        // add packet id
        byte nPacketID = (byte)((~packetID)&0xFF);
        tempArray.add(packetID);
        tempArray.add(nPacketID);
        byte len1 = (byte)((data.length & 0xFF00) >> 8);
        byte len2 = (byte)(data.length & 0x00FF);
        // add data length
        tempArray.add(len1);
        tempArray.add(len2);
        // add data
        for(int i = 0; i < data.length; i++) {
            tempArray.add(data[i]);
        }
        // add crc32
        Byte[] crc32 = create_crc32_packed((Byte[])tempArray.toArray());
        for(int i = 0; i < 4; i++) {
            tempArray.add(crc32[i]);
        }

        // convert to primitive byte array
        byte[] output = new byte[tempArray.size()];
        for(int i = 0; i < tempArray.size(); i++) {
            output[i] = tempArray.get(i).byteValue();
        }

        return output;
    }

}

package com.example.david.myapplication;

import java.util.zip.CRC32;

public class PacketTools {
    private final char SOP1 = 0x9A;
    private final char SOP2 = 0xCC;

    private CRC32 crc_generator = null;

    public byte[] floatToBytes(float in_float) {
        int int_bits = Float.floatToIntBits(in_float);
        return new byte[] {
                (byte)((int_bits & 0xFF000000L) >> 24),
                (byte)((int_bits & 0x00FF0000L) >> 16),
                (byte)((int_bits & 0x0000FF00L) >> 8),
                (byte)(int_bits & 0x000000FFL)
        };
    }

    public float bytesToFloat(byte[] in_bytes) {
        int int_bits = ((int)(in_bytes[0]) << 24) +
                ((int)(in_bytes[1]) << 16) +
                ((int)(in_bytes[2]) << 8) +
                ((int)(in_bytes[3]));

        return Float.intBitsToFloat(int_bits);
    }

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

        byte[] toGenerate = new byte[protectMe.length()];
        for (int i = 0; i < protectMe.length(); i++){
            toGenerate[i] = (byte)(protectMe.charAt(i) & 0xFF);
        }
//        byte[] toGenerate = protectMe.toString().getBytes(StandardCharsets.UTF_16);
        crc_generator.update(toGenerate);
//        crc_generator.update(protectMe.toString().getBytes(StandardCharsets.UTF_8));

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
    public StringBuffer Pack(char packetID, char[] data) {
        StringBuffer retStringB = new StringBuffer(data.length+10);

        // add SOP bytes
        retStringB.append((char)SOP1);
        retStringB.append((char)SOP2);
        // add packet id
        char nPacketID = (char)((~packetID)&0xFF);
        retStringB.append(packetID);
        retStringB.append(nPacketID);
        char len1 = (char)((data.length & 0xFF00) >> 8);
        char len2 = (char)(data.length & 0x00FF);
        // add data length
        retStringB.append(len1);
        retStringB.append(len2);
        // add data

        for(int i = 0; i < data.length; i++) {
            retStringB.append(data[i]);
        }
        // add crc32
        retStringB.append(create_crc32_packed(retStringB));

        return retStringB;
    }

    public Packet Unpack(StringBuffer raw_bytes) {
        int bytes_len = raw_bytes.length();
        int data_len = 0;
        char packet_type, nPacket_type;
        Packet retPacket = new Packet();
        retPacket.PacketID = 0;
        retPacket.SOPposition = -1;
        retPacket.PacketLength = -1;
        retPacket.Data = null;
        StringBuffer data = null;

        StringBuffer SOP_string = new StringBuffer(2);
        SOP_string.append((char)SOP1);
        SOP_string.append((char)SOP2);

        // Search for a valid SOP
        retPacket.SOPposition = raw_bytes.indexOf(SOP_string.toString());
        if(retPacket.SOPposition >= 0) {
            // SOP found! Next is packet type and data length.
            if(bytes_len > retPacket.SOPposition + 4) {
                packet_type = raw_bytes.charAt(retPacket.SOPposition + 2);
                nPacket_type = raw_bytes.charAt(retPacket.SOPposition + 3);
                if(packet_type != ((~nPacket_type)&0xFF)) {
                    // Packet type error
                    // SOP position already placed, but other data is negative.
                    // This indicates a valid SOP sequence but an invalid packet.
                    // Serial handler should skip past this first valid SOP and start
                    // search after that.
                    return retPacket;
                }
                data_len = (int) (raw_bytes.charAt(retPacket.SOPposition + 4)) << 8;
                data_len += (int) (raw_bytes.charAt(retPacket.SOPposition + 5));
                // Now check if rest of packet is good length
                if(bytes_len > retPacket.SOPposition + data_len + 10) {
                    // Enough bytes, excellent.
                    data = new StringBuffer(raw_bytes.substring(retPacket.SOPposition + 6,
                            retPacket.SOPposition + 6 + data_len));
                    // Check the CRC.
                    StringBuffer crc_local = new StringBuffer(raw_bytes.substring(retPacket.SOPposition,
                            retPacket.SOPposition+6+data_len));
                    StringBuffer crc_remote = new StringBuffer(raw_bytes.substring(retPacket.SOPposition + 7 + data_len,
                            retPacket.SOPposition + 10 + data_len));
                    if(crc_local.toString().equals(crc_remote.toString())) {
                        // CRC32 is good!
                        retPacket.PacketID = packet_type;
                        retPacket.PacketLength = 10 + data_len;
                        retPacket.Data = data;
                        return retPacket;
                    }
                }
            }
        }

        return retPacket;
    }
}


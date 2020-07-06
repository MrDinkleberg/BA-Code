public class SegmentHeader {

    public long startaddress;
    public int maxblocksize;
    public int blocks;
    public long fragmentedblocks;
    public double fragmentation;
    public long[] freeblocks;


    public SegmentHeader(long startaddress, int maxblocksize, long size) {
        this.startaddress = startaddress;
        this.blocks = (int) (size / maxblocksize + 1);
        freeblocks = new long[32];
    }

    public void initSegment(int blocks){
        firstBlock();

        lastBlock();
    }

    private void firstBlock(){      //Spezielle Funktion, da der erste Marker nur ein halbes Byte gross ist

    }

    private void lastBlock(){       //Spezielle Funktion, da der letzte Marker nur ein halbes Byte gross ist

    }



}

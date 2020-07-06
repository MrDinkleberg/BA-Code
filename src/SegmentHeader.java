public class SegmentHeader {

    public long startaddress;
    public long endaddress;
    public int maxblocksize;
    public int blocks;
    public long fragmentedblocks;
    public double fragmentation;
    public long[] freeblocks;


    public SegmentHeader(long startaddress, int maxblocksize, long size) {
        this.startaddress = startaddress;
        this.endaddress = startaddress + size;
        this.blocks = (int) (size / maxblocksize + 1);
        freeblocks = new long[32];
    }

    public long findFreeBlock(){
        return freeblocks[31];
    }




}

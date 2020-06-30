public class SegmentHeader {

    public long startaddress;
    public int blocks;
    public long fragmentedblocks;
    public double fragmentation;

    public SegmentHeader(long startaddress) {
        this.startaddress = startaddress;
    }

    public void initSegment(int blocks){

    }



}

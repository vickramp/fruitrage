
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;//scale modify value

class FindMove{
    char board[][];
    static int ival,jval;
    int best;
    boolean yes;
    static long cnt;
    static long val;
    ThreadMXBean threadMXBean;
    static  HashMap<Integer, Integer> map ;
    FindMove(char c[][]){
         threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadCpuTimeEnabled(true);
        cnt=0;
        board=c;
        val=0;
        yes=false;
        ival=jval=-1;
        best=Integer.MIN_VALUE;
    }
    void gravity(char[][]board){
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            int i=(int)pair.getValue();
            int j=(int)pair.getKey();
            int k=i;
            while(k>=0){
                if(board[k][j]!='*')
                    board[i--][j]=board[k][j];
                k--;
            }
            while (i>=0)
                board[i--][j]='*';
            it.remove();
        }
    }
    static int remove(boolean stat,int i, int j,char[][] board){
        char c=board[i][j];
        int count=1;
        board[i][j]='*';
        if(j!=0&&c==board[i][j-1])
            count+=remove(stat,i,j-1,board);
        if(i!=0&&c==board[i-1][j])
            count+=remove(stat,i-1,j,board);
        if(j!=board.length-1&&c==board[i][j+1])
            count+=remove(stat,i,j+1,board);
        if(i!=board.length-1&&c==board[i+1][j])
            count+=remove(stat,i+1,j,board);
        if(stat) {
            if (map.containsKey(j)) {
                Integer a = map.get(j);
                if (a < i)
                    map.put(j, i);
            } else
                map.put(j, i);
        }
        return count;
    }
    void next() throws IOException{
        expand(true, 0,0,0,Integer.MIN_VALUE,Integer.MAX_VALUE,10);
        try( BufferedWriter write=new BufferedWriter((new FileWriter("calibration.txt")))) {
                  write.write(Double.toString(FindMove.cnt/40));
        }
        catch (IOException e){
            System.out.println("File Write Error");
        }
           }




    int expand(boolean cur,int depth,int a,int b,int alfa,int beta,int maxdepth){
        if(threadMXBean.getThreadCpuTime(Thread.currentThread().getId())>(long)120000000000.00){

            return 0;
        }
        if(depth>=maxdepth)
            return a-b;
        cnt++;
        int t1,v=Integer.MIN_VALUE;
        char tmp[][]=new char[board.length][board.length];
        for(int i=0;i<board.length;i++)
            System.arraycopy(board[i], 0, tmp[i], 0, board[i].length);
        ArrayList<ValSet>q=generate();
        Collections.sort(q);
        Iterator<ValSet> itr = q.iterator();
        if(q.size()==0)
            return a-b;
        if(q.size()==1){
            ValSet s=itr.next();
            s.key*=s.key;
            if(depth==0)
            {
                ival=s.i;
                jval=s.j;
                return s.key;
            }
            if(cur)
                return a+s.key-b;
            else
                return a-b-s.key;
        }
        while(itr.hasNext()) {
            ValSet s=itr.next();
            map= new HashMap<>();
            remove(true,s.i,s.j,board);
            gravity(board);
            map=null;
            t1=expand(!cur, depth + 1,a+(cur?s.key*s.key:0),b+(cur?0:s.key*s.key),alfa,beta,maxdepth);
            if(threadMXBean.getThreadCpuTime(Thread.currentThread().getId())>(long)120000000000.00){
                return 0;
            }
            if (v==Integer.MIN_VALUE)
                v = t1;
            else if(cur)
                v = (v > t1) ? v : t1;
            else
                v = (v > t1) ? t1 : v;
            if(cur){
                if(v>=beta)
                    return v;
                alfa=v<alfa?alfa:v;
            }
            else {
                if (v <= alfa)
                    return v;
                beta = v < beta ? v : beta;
            }
            for (int i = 0; i < board.length; i++)
                System.arraycopy(tmp[i], 0, board[i], 0, board[i].length);
            if(depth==0&&best<v) {
                best = v;
                ival = s.i;
                jval = s.j;
            }
        }
        return v;
    }





    ArrayList<ValSet> generate(){
        ArrayList<ValSet> q=new ArrayList<>();
        char tmp[][]=new char[board.length][board.length];
        for(int i=0;i<board.length;i++)
            System.arraycopy(board[i], 0, tmp[i], 0, board[i].length);
        for(int i=0;i<board.length;i++)
            for(int j=0;j<board.length;j++)
                if(tmp[i][j]!='*')
                    q.add(new ValSet(remove(false,i,j,tmp),i,j));
        return q;
    }
}
class ValSet implements Comparable<ValSet> {
    int key;
    int i,j;
    public ValSet(int key, int i,int j) {
        this.key = key;
        this.i = i;
        this.j=j;
    }
    public int compareTo(ValSet other) {
        return other.key-this.key;
    }
}
public class calibrate {
    public static void main(String[] args) {
        try{
                char[][]c=new char[26][26];
            for(int i=0;i<26;i++)
                    c[0][i]=(char)(i%2+'0');
            for(int i=1;i<26;i++)
                for(int j=1;j<26;j++)
                    c[i][j]=(char)((c[i-1][j]-'0'+1)%10+'0');
            new FindMove(c).next();
        }
        catch(Exception e){
            System.err.println(e);
        }
    }
}

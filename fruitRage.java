import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;//scale modify value
class Move{
    char board[][];
    static int ival,jval;
    int best;
    boolean yes;
    static int n,c;
    static float t;
    static long cnt=0;
    static long nodeCount;
    static  HashMap<Integer, Integer> map ;
    Move(char c[][]){
        board=c;
        yes=false;
        ival=jval=-1;
        best=Integer.MIN_VALUE;
    }
    public static void getValues(int cnt,int len,int numstar) {
        try (BufferedReader read = new BufferedReader((new FileReader("calibration.txt")))) {
            nodeCount=(long)Double.parseDouble(read.readLine());
        }
        catch (Exception e) {
            System.out.println("\"Calibration.txt\" Missing:"+e);
            nodeCount=18000;
        }
        finally {
     		if(nodeCount<10000)
             nodeCount=10000;
            if (cnt == 0)
                return;

            nodeCount *= 676 / cnt;
          if(t>60&&cnt>40&&(((len*len)-numstar)/2<cnt))
                 t = 52.123f;
          if (t!=52.123f&& cnt < 20 && t > 30 && t <= 60) {
                if (Math.random() > 0.3)
                    t = 222;
                else
                    t = 25;
            }
            if (nodeCount > 200000&&cnt<15)
                nodeCount = 200000;
            else if (nodeCount > 100000)
                nodeCount = 100000;
        }
    }
      void show(String s) throws  IOException {
        try (BufferedWriter write = new BufferedWriter((new FileWriter("output.txt")))) {
            write.write(s.charAt(0) + "" + (Integer.parseInt(s.substring(1)) + 1));
            for (char[] c : board) {
                write.newLine();
                write.write(new String(c));
            }
        }
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
        if(j!=Move.n-1&&c==board[i][j+1])
            count+=remove(stat,i,j+1,board);
        if(i!=Move.n-1&&c==board[i+1][j])
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
    String next() throws IOException{
        ival=jval=-1;
        int c=3;
        if(t<30) {
            if (t <= 5)
                c = 1;
            else if (t <= 15 && t > 5)
                c = 2;
            else if (t >15 && t < 30)
                c = 3;
            expand(true, 0,0,0,Integer.MIN_VALUE,Integer.MAX_VALUE,c);
        }
        else{

            char [][] brd=new char[n][n];
            for(int i=0;i<n;i++)
                System.arraycopy(board[i], 0, brd[i], 0, board[i].length);
            int ai=-1,aj=-1;
            c=2;
            long pk=-1;
            if(t<100&&Math.random()<0.5) {
                if(n<15)
                expand(true, 0, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 4);
                else
                expand(true, 0, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 3);
            }
            else
            while(2*cnt<=nodeCount) {
                long val=cnt;
                yes=true;
                int k=expand(true, 0,0,0, Integer.MIN_VALUE, Integer.MAX_VALUE, ++c);
                System.out.println(c+" "+k);
               best=Integer.MIN_VALUE;
                val-=cnt;
                if(k==Integer.MIN_VALUE){
                    ival=ai;
                    jval=aj;
                }
                else{
                    ai=ival;
                    aj=jval;
                }
                if(pk==-val)
                    break;
                pk=-val;
            }
            board=brd;
        }
        if(ival==-1&&t!=1){
            yes=false;
            best=Integer.MIN_VALUE;
            t=1;
            return next();
        }
        return Character.toString((char) (jval + (int) 'A')) + Integer.toString(ival);
    }
    int expand(boolean cur,int depth,int a,int b,int alfa,int beta,int maxdepth){
        if(yes&&cnt>nodeCount)
            return Integer.MIN_VALUE;
        if(depth>=maxdepth)
            return a-b;
        cnt++;
        int t1=0,v=Integer.MIN_VALUE;
        char tmp[][]=new char[n][n];
        for(int i=0;i<n;i++)
            System.arraycopy(board[i], 0, tmp[i], 0, board[i].length);
        ArrayList<set>q=generate();
        Collections.sort(q);
        Iterator<set> itr = q.iterator();
        if(q.size()==0)
            return a-b;
        if(q.size()==1){
            set s=itr.next();
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
            set s=itr.next();
            map= new HashMap<>();
            remove(true,s.i,s.j,board);
            gravity(board);
            map=null;
            t1=expand(!cur, depth + 1,a+(cur?s.key*s.key:0),b+(cur?0:s.key*s.key),alfa,beta,maxdepth);
            if(yes&&cnt>nodeCount)
                return Integer.MIN_VALUE;
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
            for (int i = 0; i < n; i++)
                System.arraycopy(tmp[i], 0, board[i], 0, board[i].length);
            if(depth==0&&best<v) {
                best = v;
                ival = s.i;
                jval = s.j;
            }
        }
            return v;
    }
    ArrayList<set> generate(){
        ArrayList<set> q=new ArrayList<>();
        char tmp[][]=new char[n][n];
        for(int i=0;i<n;i++)
            System.arraycopy(board[i], 0, tmp[i], 0, board[i].length);
        for(int i=0;i<n;i++)
            for(int j=0;j<n;j++)
                if(tmp[i][j]!='*')
                    q.add(new set(remove(false,i,j,tmp),i,j));
        return q;
    }
}
class set implements Comparable<set> {
    int key;
    int i,j;
    public set(int key, int i,int j) {
        this.key = key;
        this.i = i;
        this.j=j;
    }
   public int compareTo(set other) {
        return other.key-this.key;
    }
}
public class homework {
    public static void main(String[] args) {
        //ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
       // threadMXBean.setThreadCpuTimeEnabled(true);
        try( BufferedReader read=new BufferedReader((new FileReader("input.txt")))) {
            Move.n= Integer.parseInt(read.readLine());
            Move.c=Integer.parseInt(read.readLine());
            Move.t=Float.parseFloat(read.readLine());
            char c[][]=new char[Move.n][Move.n];
            for(int i=0;i<Move.n;i++)
                c[i]=read.readLine().toCharArray();
            int cnt=0,cnt1=0;
            char [][]tmp=new char[c.length][c.length];
            for(int i=0;i<c.length;i++)
                System.arraycopy(c[i], 0, tmp[i], 0, c[i].length);
            for(int i=0;i<c.length;i++)
                for(int j=0;j<c.length;j++){
                  if(c[i][j]=='*')
                  cnt1++;
                    if(tmp[i][j]!='*') {
                        cnt++;
                        Move.remove(false,i,j,tmp);
                    }
                  }
            Move m=new Move(c);
            m.getValues(cnt,c.length,cnt1);
            if(cnt==0) {
                m.show("A0");
                return;
            }
            String s=m.next();
            Move.map= new HashMap<>();
            m.remove(true,Integer.parseInt(s.substring(1)),s.charAt(0)-'A',m.board);
            m.gravity(m.board);
            m.show(s);
         //   System.out.println(threadMXBean.getThreadCpuTime(Thread.currentThread().getId())/1000000000.00);
        }
        catch(IOException e){
            System.err.println(e);
        }
    }
}

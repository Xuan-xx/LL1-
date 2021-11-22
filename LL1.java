import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class LL1 {
    private static HashMap<Character, String> rule = rule();
    private static HashSet<Character> vt = vt();
    private static HashSet<Character> vn = vn();
    private static HashMap<Character, HashSet<Character>> first = getAllFirst();
    private static HashMap<Character, HashSet<Character>> follow = getAllFollow();
    private static String[][] table ;

    //    文法
    //    E'-e,T'-t,null-0
    public static HashMap<Character, String> rule() {
        HashMap<Character, String> map = new HashMap<Character, String>();
        map.put('E', "Te");
        map.put('e', "+Te|0");
        map.put('T', "Ft");
        map.put('t', "*Ft|0");
        map.put('F', "(E)|i");
        return map;
    }

    //    终结符
    public static HashSet<Character> vt() {
        HashSet<Character> vt = new HashSet<>();
        vt.add('+');
        vt.add('*');
        vt.add('i');
        vt.add('(');
        vt.add(')');
        vt.add('0');
        return vt;
    }

    //    非终结符
    public static HashSet<Character> vn() {
        HashSet<Character> vn = new HashSet<>();
        vn.add('E');
        vn.add('e');
        vn.add('T');
        vn.add('t');
        vn.add('F');
        return vn;
    }

    public static HashMap<Character, HashSet<Character>> getAllFirst() {
        HashMap<Character, HashSet<Character>> first = new HashMap<>();
//        非终结符
        for (Character c :
                vn) {
            String str = rule.get(c);
            first.put(c, findFirst(str));
        }
//        终结符
        for (Character c :
                vt) {
            HashSet<Character> tmp = new HashSet<>();
            tmp.add(c);
            first.put(c, tmp);
        }
        return first;
    }

    public static HashSet<Character> findFirst(String str) {
        HashSet<Character> first = new HashSet<>();
        String[] strArray = str.split("\\|");//分隔’｜’
        for (String s :
                strArray) {
            Character c = s.charAt(0);
            if (vt.contains(c) || c == '0') {
                first.add(c);
            } else if (vn.contains(c)) {
                first = findFirst(rule.get(c));
                first.remove('0');//去除空
            }
        }
        return first;
    }

    public static HashMap<Character, HashSet<Character>> getAllFollow() {
        HashMap<Character, HashSet<Character>> follow = new HashMap<>();

        //用first集得到follow集
        for (Character c :
                vn) {
            String str = rule.get(c);
            String[] strArray = str.split("\\|");
            for (String s :
                    strArray) {
                if (!s.equals("0") && s.length() >= 2) {
                    HashSet<Character> set = first.get(s.charAt(s.length() - 1));
                    set.remove('0');
                    follow.put(s.charAt(s.length() - 2), set);
                }
            }
        }
        follow.get('E').add('#');

//        记录各个follow的长度
        HashMap<Character, Integer> follow_size = new HashMap<>();
        boolean flag = true;
        for (Map.Entry<Character, HashSet<Character>> e :
                follow.entrySet()) {
            follow_size.put(e.getKey(), e.getValue().size());
        }

        while (true) {
            flag = true;
            for (Character c :
                    vn) {
                String str = rule.get(c);
                String[] strArray = str.split("\\|");
                for (String s : strArray) {
                    int tail = s.length() - 1;
//                    末尾为非终结符
                    if (vn.contains(s.charAt(tail))) {
                        HashSet<Character> set = follow.get(s.charAt(tail));
                        if (set != null) {
                            HashSet<Character> tmp = follow.get(c);
                            tmp.remove('0');
                            set.addAll(tmp);
                        } else {
                            HashSet<Character> tmp = follow.get(c);
                            tmp.remove('0');
                            follow.put(s.charAt(tail), tmp);
                        }
                    }

//                  末尾可推导出空
                    if (s.length() >= 2 && vn.contains(s.charAt(tail - 1)) && rule.get(s.charAt(tail)) != null && rule.get(s.charAt(tail)).contains("|0")) {
                        HashSet<Character> set = follow.get(s.charAt(tail - 1));
                        if (set != null) {
                            HashSet<Character> tmp = follow.get(c);
                            tmp.remove('0');
                            set.addAll(tmp);
                        } else {
                            HashSet<Character> tmp = follow.get(c);
                            tmp.remove('0');
                            follow.put(s.charAt(tail - 1), tmp);
                        }
                    }
                }
            }
//            检测follow是否有变化
            for (Map.Entry<Character, HashSet<Character>> e :
                    follow.entrySet()) {
                if (follow_size.get(e.getKey()) == null) {
                    follow_size.put(e.getKey(), e.getValue().size());
                    flag = false;
                    continue;
                }
                if (follow_size.get(e.getKey()) != e.getValue().size()) {
                    follow_size.put(e.getKey(), e.getValue().size());
                    flag = false;
                }
            }
            if (flag)
                break;
        }
        return follow;
    }

    public static String[][] initTable(){
        vt.remove('0');
        vt.add('#');
        for (Map.Entry<Character,String> e:
             rule.entrySet()) {
            if (e.getValue().contains("e")){
                rule.put(e.getKey(),e.getValue().replaceAll("e","E'"));
            }
            if (e.getValue().contains("t")){
                rule.put(e.getKey(),e.getValue().replaceAll("t","T'"));
            }
        }
        String[][] result = new String[vn.size()][vt.size()];
        int i = 0,j = 0;
        for (Character n :
                vn) {
            boolean flag = false;
            if (rule.get(n).contains("0")){
                flag = true;
            }
            for (Character t :
                    vt) {
                if (flag && follow.get(n).contains(t)){
                    if (n=='e')
                        result[i][j] = "E'" +"->"+"0";
                    else if (n=='t')
                        result[i][j] = "T'" +"->"+"0";
                    else
                        result[i][j] = n +"->"+"0";
                }

                if (first.get(n).contains(t)){
                    if (rule.get(n).contains("|")){
                        String[] strArray = rule.get(n).split("\\|");
                        for (String s:
                             strArray) {
                            if(s.charAt(0)==t){
                                if (n=='e')
                                    result[i][j] = "E'" +"->"+s;
                                else if (n=='t')
                                    result[i][j] = "T'" +"->"+s;
                                else
                                    result[i][j] = n +"->"+s;
                            }
                        }
                    }else {
                        if (n=='e')
                            result[i][j] = "E'" +"->"+rule.get(n);
                        else if (n=='t')
                            result[i][j] = "T'" +"->"+rule.get(n);
                        else
                            result[i][j] = n +"->"+rule.get(n);
                    }
                }

                j++;
            }
            j=0;
            i++;
        }

        for(i = 0;i<result.length;i++){
            for (j = 0;j<result[0].length;j++){
                if (result[i][j]==null){
                    result[i][j] = "\t";
                }
            }
        }
        return result;
    }

    public static void showTable(){
        System.out.println();
        int i = 0,j=0;
        for (Character c :
                vn) {
            if (c=='e')
                System.out.print("E'"+"\t");
            else if (c=='t')
                System.out.print("T'"+"\t");
            else
                System.out.print(c+"\t");
            for (Character t :
                    vt) {
                System.out.print(t+":"+table[i][j]+"\t\t");
                j++;
            }
            j=0;
            System.out.println();
            i++;
        }
    }

    public static void main(String[] args) {
        System.out.println("First:"+getAllFirst());
        System.out.println("Follow:"+getAllFollow());
        table = initTable();
        showTable();
    }
}

package gitlet;

import java.io.File;

import static gitlet.Utils.join;
/*use the branch name as file name and file contain the hashCommit of that branch's head point*/
public class RefsControl {
    public static final File REFS_DIR = join(Repository.GITLET_DIR, "refs");
    public static void storeHead(String hashCommit, String currentHead){
        File HEAD = join(REFS_DIR,currentHead);
        Utils.writeContents(HEAD,hashCommit);
    }
    public static String returnHead(String currentHead){
        File HEAD = join(REFS_DIR,currentHead);
        return Utils.readContentsAsString(HEAD);
    }
}

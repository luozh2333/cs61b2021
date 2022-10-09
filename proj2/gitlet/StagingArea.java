package gitlet;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static gitlet.Utils.join;

public class StagingArea implements Serializable {
    public static final File STAGINGAREA = join(Repository.GITLET_DIR, "stagingArea");
    private String CurrentHead;

    private  HashMap<String,String> referenceForAddition;
    private HashMap<String,String> referenceForRemove;
    public StagingArea(){
        referenceForAddition = new HashMap<>();
        referenceForRemove = new HashMap<>();
    }
    public void addAdditionHelp(String name, String id){
        referenceForAddition.put(name,id);
    }
    public void addRemoveHelp(String name, String id){
        referenceForRemove.put(name,id);
    }
    public  String GetAdditionId(String Filename){
        return this.referenceForAddition.get(Filename);
    }
    public  String GetRemoveId(String Filename){
        return this.referenceForRemove.get(Filename);
    }

    public Boolean IsAdditionEmpty(){
        return referenceForAddition.isEmpty();
    }
    public Boolean IsRemoveEmpty(){
        return referenceForRemove.isEmpty();
    }
    public void removeAddition(String name){
         referenceForAddition.remove(name);
    }
    public void removeRemove(String name){
        referenceForRemove.remove(name);
    }


    public static StagingArea fromFile(){
        return Utils.readObject(STAGINGAREA,StagingArea.class);
    }
    public static void toFile(StagingArea updateSA){
        Utils.writeObject(STAGINGAREA,updateSA);
    }
    public static void createdNewStagingArea(String head){
        StagingArea newSA = new StagingArea();
        newSA.SetCurrentHead(head);
        Utils.writeObject(STAGINGAREA,newSA);
    }
    public  Set<String> KetSetForAddition(){
        return  this.referenceForAddition.keySet();
    }
    public  Set<String> KetSetForRemove(){
        return  this.referenceForRemove.keySet();
    }
    public void SetCurrentHead(String Head){
        CurrentHead = Head;
    }
    public String  GetCurrentHead(){
        return CurrentHead;
    }


}

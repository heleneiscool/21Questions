// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2020T2, Assignment 4
 * Name:
 * Username:
 * ID:
 */

/**
 * Implements a decision tree that asks a user yes/no questions to determine a decision.
 * Eg, asks about properties of an animal to determine the type of animal.
 * 
 * A decision tree is a tree in which all the internal nodes have a question, 
 * The answer to the question determines which way the program will
 *  proceed down the tree.  
 * All the leaf nodes have the decision (the kind of animal in the example tree).
 *
 * The decision tree may be a predermined decision tree, or it can be a "growing"
 * decision tree, where the user can add questions and decisions to the tree whenever
 * the tree gives a wrong answer.
 *
 * In the growing version, when the program guesses wrong, it asks the player
 * for another question that would help it in the future, and adds it (with the
 * correct answers) to the decision tree. 
 *
 */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.awt.Color;

public class DecisionTree {

    public DTNode theTree;    // root of the decision tree;

    /**
     * Setup the GUI and make a sample tree
     */
    public static void main(String[] args){
        DecisionTree dt = new DecisionTree();
        dt.setupGUI();
        dt.loadTree("sample-animal-tree.txt");
    }

    /**
     * Set up the interface
     */
    public void setupGUI(){
        UI.addButton("Load Tree", ()->{loadTree(UIFileChooser.open("File with a Decision Tree"));});
        UI.addButton("Print Tree", this::printTree);
        UI.addButton("Run Tree", this::runTree);
        UI.addButton("Grow Tree", this::growTree);
        UI.addButton("Save Tree", ()->{saveTree(UIFileChooser.open("File to save to:"));});  // for completion
        UI.addButton("Draw Tree", this::drawTree);  // for challenge
        UI.addButton("Reset", ()->{loadTree("sample-animal-tree.txt");});
        UI.addButton("Load saved",()->{loadTreeChallenge(UIFileChooser.open("File to load from"));}); 
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.5);
    }

    /**  
     * Print out the contents of the decision tree in the text pane.
     * The root node should be at the top, followed by its "yes" subtree, and then
     * its "no" subtree.
     * Each node should be indented by how deep it is in the tree.
     * Needs a recursive "helper method" which is passed a node and an indentation string.
     *  (The indentation string will be a string of space characters)
     */
    public void printTree(){
        UI.clearText();
        doPrintTree(theTree,"", "");

    }

    public void doPrintTree(DTNode top, String layer, String prefix){
        //if(top==null){return;}
        UI.print(layer);
        UI.print(prefix);
        UI.println(top.getText());
        Map<String, DTNode> nodes = new HashMap<String, DTNode>(top.getAllNodes());
        if(nodes==null){return;}
        for(Map.Entry<String, DTNode> entry: nodes.entrySet()){      
            doPrintTree(entry.getValue(), layer+"    ", entry.getKey()+" ");
        }
        //doPrintTree(top.getYes(),1, layer+"    ");
        //doPrintTree(top.getNo(),-1,layer+"    ");
    }

    /**
     * Run the tree by starting at the top (of theTree), and working
     * down the tree until it gets to a leaf node (a node with no children)
     * If the node is a leaf it prints the answer in the node
     * If the node is not a leaf node, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     */
    public void runTree() {
        DTNode current = theTree;
        while(!current.isAnswer()){
            String ans = UI.askString(current.getText()+current.getAllNodes().keySet());
            HashMap<String, DTNode> nodes = new HashMap<String, DTNode>(current.getAllNodes());
            if(nodes.keySet().contains(ans)){
                current = nodes.get(ans);
             }
        }
        UI.println("Answer is: "+current.getText());
    }

    /**
     * Grow the tree by allowing the user to extend the tree.
     * Like runTree, it starts at the top (of theTree), and works its way down the tree
     *  until it finally gets to a leaf node. 
     * If the current node has a question, then it asks the question in the node,
     * and depending on the answer, goes to the "yes" child or the "no" child.
     * If the current node is a leaf it prints the decision, and asks if it is right.
     * If it was wrong, it
     *  - asks the user what the decision should have been,
     *  - asks for a question to distinguish the right decision from the wrong one
     *  - changes the text in the node to be the question
     *  - adds two new children (leaf nodes) to the node with the two decisions.
     */
    public void growTree () {
        DTNode current = theTree;
        String ans = "";
        while(true){
            Set<String> answers = current.getAllNodes().keySet();
            ans = UI.askString(current.getText()+answers);
            if(!answers.contains(ans)){
                String name = UI.askString("What were you thinking of: ");
                current.addNode(ans, new DTNode(name));
                return;
            }
            HashMap<String, DTNode> nodes = new HashMap<String, DTNode>(current.getAllNodes());
            if(nodes.keySet().contains(ans)){
                if(nodes.get(ans). isAnswer()){break;}
                current = nodes.get(ans);
            }

        }
        DTNode next = current.getNodeAt(ans);
        Boolean correct = UI.askBoolean("Is the answer: "+next.getText());
        if(correct){return;}
        String expectedAns = UI.askString("What should the answer be? ");
        DTNode newNode = new DTNode(UI.askString("How can you distingish between "+next.getText()+" and "+expectedAns));

        HashMap<String, DTNode> newChildren = new HashMap<String, DTNode>();
        newChildren.put(UI.askString("answer for: "+next.getText()), next);
        newChildren.put(UI.askString("answer for: "+expectedAns), new DTNode(expectedAns));
        newNode.setChildren(newChildren);

        current.addNode(ans, newNode);
    }

    // You will need to define methods for the Completion and Challenge parts.

    public void saveTree(String filename) { 
        try{
            String toPrint = saveSubTree(theTree);
            UI.println(toPrint);
            Files.writeString(Path.of(filename), toPrint);

        }
        catch(IOException e){UI.println("File reading failed: " + e);}
    }

    public String saveSubTree(DTNode top){
        if(top==null){return "";}
        if(top.isAnswer()){
            return "Answer: "+top.getText()+"\n";
        }
        String result = "Question: "+top.getText()+"\n"+top.getAllNodes().size()+"\n";
        HashMap<String, DTNode> nodes = new HashMap<String, DTNode>(top.getAllNodes());
        for(Map.Entry<String, DTNode> entry : nodes.entrySet()){
            result += entry.getKey()+"\n"+saveSubTree(entry.getValue());
        }
        return result;
    }

    public void drawTree(){
        drawSubTree(theTree, 200.0, 200.0, 0, 1);
    }

    public void drawSubTree(DTNode top, Double x, Double y, int type, int split){
        if(top==null){return;}

        top.draw(x, y);
        HashMap<String, DTNode> nodes = new HashMap<String, DTNode>(top.getAllNodes());
        if(nodes==null){return;}
        double yChange = (300.0/split)/nodes.size();
        double currY =y+(150.0/split)/2;
        for(DTNode node : nodes.values()){
            UI.drawLine(x,y-(15/2)*(Math.abs(y-currY)/(y-currY)), x+100, currY); 
            drawSubTree(node, x+100, currY, 1, split*nodes.size());
            currY-=yChange;
        }
    }

    /** 
     * Loads a decision tree from a file.
     * Each line starts with either "Question:" or "Answer:" and is followed by the text
     * Calls a recursive method to load the tree and return the root node,
     *  and assigns this node to theTree.
     */
    public void loadTree (String filename) { 
        if (!Files.exists(Path.of(filename))){
            UI.println("No such file: "+filename);
            return;
        }
        try{theTree = loadSubTree(new ArrayDeque<String>(Files.readAllLines(Path.of(filename))));}
        catch(IOException e){UI.println("File reading failed: " + e);}
    }

    /**
     * Loads a tree (or subtree) from a Scanner and returns the root.
     * The first line has the text for the root node of the tree (or subtree)
     * It should make the node, and 
     *   if the first line starts with "Question:", it loads two subtrees (yes, and no)
     *    from the scanner and add them as the  children of the node,
     * Finally, it should return the  node.
     */
    public DTNode loadSubTree(Queue<String> lines){
        Scanner line = new Scanner(lines.poll());
        String type = line.next();
        String text = line.nextLine().trim();
        DTNode node = new DTNode(text);
        if (type.equals("Question:")){
            DTNode yesCh = loadSubTree(lines);
            DTNode noCh = loadSubTree(lines);
            HashMap<String, DTNode> children = new HashMap<String, DTNode>();
            children.put("yes", yesCh);
            children.put("no", noCh);
            node.setChildren(children);
        }
        return node;

    }
    // Written for you
    /** 
     * Loads a decision tree from a file.
     * Each line starts with either "Question:" or "Answer:" and is followed by the text
     * Calls a recursive method to load the tree and return the root node,
     *  and assigns this node to theTree.
     */
    public void loadTreeChallenge (String filename) { 
        if (!Files.exists(Path.of(filename))){
            UI.println("No such file: "+filename);
            return;
        }
        try{theTree = loadSubTreeChall(new ArrayDeque<String>(Files.readAllLines(Path.of(filename))));}
        catch(IOException e){UI.println("File reading failed: " + e);}
    }

    /**
     * Loads a tree (or subtree) from a Scanner and returns the root.
     * The first line has the text for the root node of the tree (or subtree)
     * It should make the node, and 
     *   if the first line starts with "Question:", it loads two subtrees (yes, and no)
     *    from the scanner and add them as the  children of the node,
     * Finally, it should return the  node.
     */
    public DTNode loadSubTreeChall(Queue<String> lines){
        Scanner line = new Scanner(lines.poll());
        String type = line.next();
        String text = line.nextLine().trim();
        DTNode node = new DTNode(text);
        if (type.equals("Question:")){
            int numChildren = Integer.valueOf(lines.poll());
            HashMap<String, DTNode> children = new HashMap<String, DTNode>();
            for(int i=0; i<numChildren; i++){
                String ans = lines.poll();
                DTNode child = loadSubTreeChall(lines);
                children.put(ans, child);
            }
            node.setChildren(children);
        }
        return node;

    }

}

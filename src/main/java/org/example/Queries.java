package org.example;

import java.io.*;
import java.util.ArrayList;

public class Queries {
    private ArrayList<String> queries;
    private ArrayList<String[]> variables;

    public Queries() {
        queries = new ArrayList<>();
        variables = new ArrayList<>();
    }
    public void add(Object obj) {
        if(obj instanceof String)
            queries.add((String) obj);
        else
            variables.add((String[]) obj);
    }
    public String getQuery(int i) {
        return queries.get(i);
    }
    public String[] getVariables(int i) {
        return variables.get(i);
    }
    public int numOfQueries() {
        return queries.size();
    }
    public int numOfVariables(int i){
        return variables.get(i).length;
    }

    public void readQueries() {
        StringBuilder currentQuery = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(CONSTANTS.queriesFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.equals("##next")) {
                    add(currentQuery.toString());
                    currentQuery.setLength(0);

                }else if (line.startsWith("##var")) {
                    add(line.substring(line.indexOf(" ")+1).split("\\s+"));
                }else
                    currentQuery.append(line).append("\n");
            }
        }catch (Exception e) {
            e.printStackTrace();}
    }


    public void writeQueryResults(int i, ArrayList<ArrayList<String>> results)  {
        try {
            File file = new File(String.format("%s_%d.csv", CONSTANTS.resultsFile, i));
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (String var : variables.get(i))
                bw.write(var + ",");
            bw.newLine();
            for(ArrayList<String> record : results) {
                for(String binding : record)
                    bw.write(binding + ",");
                bw.newLine();
            }
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
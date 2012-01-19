package miner.main;

import miner.IGoldMiner;
import miner.IGoldMinerImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public class MineAssociationRules {

    public static void main(String[] args) {
        try {
            IGoldMiner goldMiner = new IGoldMinerImpl();
            goldMiner.mineAssociationRules();
            goldMiner.disconnect();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

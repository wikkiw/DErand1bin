package cz.viktorin.algorithm;

import cz.viktorin.model.tf.Individual;
import cz.viktorin.model.tf.TestFunction;
import java.util.List;

/**
 * Created by jakub on 27/10/15.
 */
public interface Algorithm {
    
    Individual runAlgorithm();

    List<? extends Individual> getPopulation();

    TestFunction getTestFunction();

    default Individual getBest() {
        Individual best = getPopulation().get(0);
        for (Individual individual : getPopulation()) {
            if (individual.fitness < best.fitness) best = individual;
        }
        return best;
    }

    String getName();
}

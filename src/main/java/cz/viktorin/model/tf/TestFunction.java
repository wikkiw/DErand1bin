package cz.viktorin.model.tf;

/**
 * Basic interface for test functions.
 * Created by adam on 08/04/21.
 */
public interface TestFunction {
    
    double fitness(Individual individual);

    double fitness(double[] vector);

    void constrain(Individual individual);

    double[] generateTrial(int dim);

    double fixedAccLevel();

    double optimum();
    
    double[] optimumPosition();

    double max(int dim);

    double min(int dim);
    
    String name();

}

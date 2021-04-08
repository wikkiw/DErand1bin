package cz.viktorin.model.tf;

/**
 * Created by adam on 08/04/21.
 */
public class IndividualUtil {

    public static Individual clipInBounds(Individual individual, double min, double max) {
        for (int d = 0; d < individual.vector.length; d++)
            if (individual.vector[d] > max) individual.vector[d] = max;
            else if (individual.vector[d] < min) individual.vector[d] = min;
        return individual;
    }

    public static Individual randIfOutOfBounds(Individual individual, double min, double max) {
        for (int d = 0; d < individual.vector.length; d++)
            if (individual.vector[d] > max || individual.vector[d] < min)
                individual.vector[d] = RandomUtil.nextDouble(min, max);
        return individual;
    }
    
}

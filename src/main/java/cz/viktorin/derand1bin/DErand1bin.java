package cz.viktorin.derand1bin;

import cz.viktorin.algorithm.Algorithm;
import cz.viktorin.model.tf.Cec2020;
import cz.viktorin.model.tf.Individual;
import cz.viktorin.model.tf.Random;
import cz.viktorin.model.tf.TestFunction;
import cz.viktorin.model.tf.UniformRandom;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author adam 3/11/2015
 */
public class DErand1bin implements Algorithm {

    protected int D;
    protected int G;
    protected int NP;
    protected List<Individual> P;
    protected int FES;
    protected int MAXFES;
    protected TestFunction tf;
    protected Individual best;
    protected List<Individual> bestHistory;
    protected Random rndGenerator;
    protected int id;
    protected double F;
    protected double CR;
    
    /**
     * Population diversity
     */
    List<Double> P_div_history;

    public DErand1bin(int D, int NP, int MAXFES, TestFunction f, Random rndGenerator, double F, double CR) {
        this.D = D;
        this.G = 0;
        this.NP = NP;
        this.MAXFES = MAXFES;
        this.tf = f;
        this.rndGenerator = rndGenerator;
        this.id = 0;
        this.F = F;
        this.CR = CR;
    }
    
    /**
     * Population diversity according to Polakova
     * @param pop
     * @return 
     */
    public double calculateDiversity(List<Individual> pop) {
        
        if(pop == null || pop.isEmpty()) {
            return -1;
        }
        
        double[] means = new double[this.D];
        for(int i = 0; i < this.D; i++) {
              means[i] = 0;  
        }
        pop.stream().forEach((ind) -> {
            for(int i = 0; i < this.D; i++) {
                means[i] += (ind.vector[i]/(double) pop.size());
            }
        });
        
        double DI = 0;
        
        for(Individual ind : pop) {
            for(int i = 0; i < this.D; i++) {
                DI += Math.pow(ind.vector[i] - means[i], 2);
            }
        }
        
        DI = Math.sqrt((1.0 / (double) pop.size())*DI);
        
        
        return DI;
        
    }

    @Override
    public Individual runAlgorithm() {

        /**
         * Initial population
         */
        initializePopulation();
        if (checkFES()) {
            return best;
        }

        List<Individual> newPop;
        Individual x, trial;
        double[] u, v;
        Individual[] parrentArray;

        /**
         * Diversity
         */
        this.P_div_history = new ArrayList<>();
        this.P_div_history.add(this.calculateDiversity(this.P));
        
        /**
         * generation itteration
         */
        while (true) {

            G++;
            newPop = new ArrayList<>();

            /**
             * Iteration through all individuals in generation.
             */
            for (int xIter = 0; xIter < NP; xIter++) {

                /**
                 * Parent selection
                 */
                parrentArray = getParents(xIter);
                x = parrentArray[0];

                /**
                 * Mutation
                 */
                v = mutation(parrentArray, F);

                /**
                 * Crossover
                 */
                u = crossover(x.vector, v, CR);

                /**
                 * Constrain check
                 */
                u = constrainCheck(u, x.vector);
                
                /**
                 * Trial
                 */
                trial = new Individual(x.id, u, tf.fitness(u));

                /**
                 * New generation building
                 */
                if (trial.fitness < x.fitness) {
                    newPop.add(trial);
                } else {
                    newPop.add(x);
                }
                
                this.FES++;
                this.isBest(trial);
                this.writeHistory();
                if (checkFES()) {
                    break;
                }

            }
            
            if (checkFES()) {
                break;
            }

            P = newPop;
            
            /**
             * Diversity and clustering
             */
            this.P_div_history.add(this.calculateDiversity(this.P));

        }
        
        return best;
    }
    
    /**
     * Writes population diversity history into a file
     * 
     * @param path 
     */
    public void writePopDiversityHistory(String path) {
        
        try {
            PrintWriter writer = new PrintWriter(path, "UTF-8");
            
            writer.print("{");
            
            for(int i = 0; i < this.P_div_history.size(); i++) {
                
                
                writer.print(String.format(Locale.US, "%.10f", this.P_div_history.get(i)));
                
                if(i != this.P_div_history.size()-1) {
                    writer.print(",");
                }
                
            }
            
            writer.print("}");
            
            writer.close();
            
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(DErand1bin.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }

    /**
     * 
     * @param u
     * @param x
     * @return 
     */
    protected double[] constrainCheck(double[] u, double[] x){
        /**
         * Constrain check
         */
        for (int d = 0; d < this.D; d++) {
            if (u[d] < this.tf.min(this.D)) {
                u[d] = (this.tf.min(this.D) + x[d]) / 2.0;
            } else if (u[d] > this.tf.max(this.D)) {
                u[d] = (this.tf.max(this.D) + x[d]) / 2.0;
            }
        }
        
        return u;
    }
    
    /**
     *
     * @param x
     * @param u
     * @param CR
     * @return
     */
    protected double[] crossover(double[] x, double[] u, double CR) {

        double[] v = new double[D];
        int jrand = rndGenerator.nextInt(D);

        for (int i = 0; i < D; i++) {

            if (i == jrand || rndGenerator.nextDouble() < CR) {
                v[i] = u[i];
            } else {
                v[i] = x[i];
            }

        }

        return v;

    }

    protected void constrain(Individual individual){
        
        tf.constrain(individual);
        
    }
    
    /**
     *
     * @param parentArray
     * @param F
     * @return
     */
    protected double[] mutation(Individual[] parentArray, double F) {

        double[] u = new double[D];
        double[] a = parentArray[1].vector;
        double[] b = parentArray[2].vector;
        double[] c = parentArray[3].vector;

        for (int i = 0; i < D; i++) {

            u[i] = a[i] + F * (b[i] - c[i]);

        }

        return u;

    }

    /**
     *
     * List of parents for mutation x, a, b, c
     *
     * @param xIndex
     * @return
     */
    protected Individual[] getParents(int xIndex) {

        int r1, r2, r3;

        r1 = rndGenerator.nextInt(NP);
        
        while(r1 == xIndex){
            r1 = rndGenerator.nextInt(NP);
        }
        
        r2 = rndGenerator.nextInt(NP);

        while (r2 == r1 || r2 == xIndex) {
            r2 = rndGenerator.nextInt(NP);
        }
        
        r3 = rndGenerator.nextInt(NP);

        while (r3 == r2 || r3 == r1 || r3 == xIndex) {
            r3 = rndGenerator.nextInt(NP);
        }
        
        Individual[] parrentArray = new Individual[4];

        parrentArray[0] = P.get(xIndex);
        parrentArray[1] = P.get(r1);
        parrentArray[2] = P.get(r2);
        parrentArray[3] = P.get(r3);

        return parrentArray;

    }

    /**
     * Creation of initial population.
     */
    protected void initializePopulation(){
        
        /**
         * Initial population
         */
        id = 0;
        double[] features = new double[this.D];
        this.P = new ArrayList<>();
        Individual ind;

        for (int i = 0; i < this.NP; i++) {
            id = i;
            features = this.tf.generateTrial(this.D).clone();
//            features = new double[this.D];
//            for(int j = 0; j < this.D; j++){
//                features[j] = this.rndGenerator.nextDouble(this.f.min(this.D), this.f.max(this.D));
//            }
            ind = new Individual(String.valueOf(id), features, this.tf.fitness(features));
            this.isBest(ind);
            this.P.add(ind);
            this.FES++;
            this.writeHistory();
        }
        
    }

    /**
     *
     * @return
     */
    protected boolean checkFES() {
        return (FES > MAXFES);
    }

    /**
     *
     * @param vector
     * @return
     */
    protected Individual makeIndividualFromVector(double[] vector) {

        Individual ind = new Individual();
        ind.id = String.valueOf(id);
        id++;
        ind.vector = vector;
        constrain(ind);
        ind.fitness = tf.fitness(vector);
        FES++;
        isBest(ind);
        writeHistory();

        return (ind);
    }

    /**
     *
     */
    protected void writeHistory() {
        if (bestHistory == null) {
            bestHistory = new ArrayList<>();
        }
        bestHistory.add(best);
    }

    /**
     *
     * @param ind
     * @return
     */
    protected boolean isBest(Individual ind) {

        if (best == null || ind.fitness < best.fitness) {
            best = ind;
            return true;
        }

        return false;

    }

    @Override
    public List<? extends Individual> getPopulation() {
        return P;
    }

    @Override
    public TestFunction getTestFunction() {
        return tf;
    }

    @Override
    public String getName() {
        return "DErand1bin";
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public int getD() {
        return D;
    }

    public void setD(int D) {
        this.D = D;
    }

    public int getG() {
        return G;
    }

    public void setG(int G) {
        this.G = G;
    }

    public int getNP() {
        return NP;
    }

    public void setNP(int NP) {
        this.NP = NP;
    }

    public List<Individual> getP() {
        return P;
    }

    public void setP(List<Individual> P) {
        this.P = P;
    }

    public int getFES() {
        return FES;
    }

    public void setFES(int FES) {
        this.FES = FES;
    }

    public int getMAXFES() {
        return MAXFES;
    }

    public void setMAXFES(int MAXFES) {
        this.MAXFES = MAXFES;
    }

    public TestFunction getTf() {
        return tf;
    }

    public void setTf(TestFunction f) {
        this.tf = f;
    }

    public List<Individual> getBestHistory() {
        return bestHistory;
    }

    public void setBestHistory(List<Individual> bestHistory) {
        this.bestHistory = bestHistory;
    }

    public Random getRndGenerator() {
        return rndGenerator;
    }

    public void setRndGenerator(Random rndGenerator) {
        this.rndGenerator = rndGenerator;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getF() {
        return F;
    }

    public void setF(double F) {
        this.F = F;
    }

    public double getCR() {
        return CR;
    }

    public void setCR(double CR) {
        this.CR = CR;
    }
    //</editor-fold>
    
    public static void exportResult(String name, double errValue) throws FileNotFoundException, UnsupportedEncodingException {
        
        try (PrintWriter wr = new PrintWriter(name, "UTF-8")) {
            wr.print(String.format(Locale.US, "%.16f", errValue));
        }
        
    }
    
    public static void main(String[] args) throws Exception {
    
        /**
         * handle arguments
         */
        if(args.length != 5) {
            System.out.println("You have to specify all arguments [prefix] [f] [dim] [fes] [runID]");
            return;
        }
        
        String prefix= args[0];
        int func = Integer.parseInt(args[1]);
        int dimension = Integer.parseInt(args[2]);
        int MAXFES = Integer.parseInt(args[3]);
        int runID = Integer.parseInt(args[4]);
        
        int NP = 100;
        TestFunction tf = new Cec2020(dimension, func);
        Random generator = new UniformRandom();
        double f = 0.5, cr = 0.8;

        Algorithm de;
        de = new DErand1bin(dimension, NP, MAXFES, tf, generator, f, cr);
        de.runAlgorithm();
        
        System.out.println(new Date());
        System.out.println("Run ID: " + runID);
        double best = (de.getBest().fitness - tf.optimum());
        System.out.println("Result error: " + best);
        String fileName = prefix + "CEC2020_f" + func + "_d" + dimension + "_run-" + runID + ".txt";
        exportResult(fileName, best);
        
    }

}

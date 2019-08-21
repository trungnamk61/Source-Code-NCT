package com.agriculture.nct.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/** 
 x contain variables
 y is the predict variables
 */
public class LinearRegression {

	private int index, numberOfInstance;
	private ArrayList<Instances> x;
	private Instances y;
	private ArrayList<Double> w;
	private String name;
	
	public LinearRegression(String filePath, int index) {
		// TODO Auto-generated constructor stub
		try {
//			this.x = new ArrayList<Instances>();
			this.w = new ArrayList<Double>();
			ArrayList<Instances> xTemp = new ArrayList<Instances>();
			ArrayList<Double> wTemp = new ArrayList<Double>();
			
			// add x0 to array
			Instances x0 = new Instances("x0");
			xTemp.add(x0);
			this.index = index;
			this.numberOfInstance = readFile(filePath, xTemp);
			
			// Pass predict value to y variable
			y = xTemp.get(index);
			xTemp.remove(index);
			
			// set x0's all elements to 1
			for(int i = 0; i < numberOfInstance; i ++) {
				xTemp.get(0).addData(1);
			}
			
			for(int i = 0; i < xTemp.size(); i++) {
				w.add((double) (i + 1));
			}
			this.x = xTemp;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Calculate w with formular W = inverse(XT*X)*XT*y
	public void trainWithNormalEquation() {	
		
		// co the? vie't nga'n lai bang` 1 ham` nhan matran
		double[][] XTX = new double[x.size()][x.size()];
		for(int j = 0; j < x.size(); j++) {
			for(int a = 0; a < x.size(); a++) {
				double temp = 0;
				for(int i = 0; i < numberOfInstance; i++) {
					temp += x.get(j).get(i)*x.get(a).get(i);
				}
				XTX[j][a] = temp;
			}
		}
		
		// co the? vie't nga'n lai bang` 1 ham` nhan matran
		double[][] invXTX = invert(XTX);
		double[][] invXTXxXT = new double[x.size()][numberOfInstance];
		for(int i = 0; i < x.size(); i++) {
			for(int j = 0; j < numberOfInstance; j++) {
				double temp = 0;
				for(int a = 0; a < x.size(); a++) {
					temp += invXTX[i][a]*x.get(a).get(j); 
				}
				invXTXxXT[i][j] = temp;
			}
		}
		
		// co the? vie't nga'n lai bang` 1 ham` nhan matran
		for(int i = 0; i < x.size(); i++) {
			double temp = 0;
			for(int j = 0; j < numberOfInstance; j++) {
				temp += invXTXxXT[i][j]*y.get(j);
			}
			w.set(i, temp);
		}
	}
	
	// Add declare convergence if cost function decrease by less than 10^-3
	// Train use gradient descent
	public void trainWithGradientDescent(double learningRate) {
		// Calculate ho(x) - y for each instance
		double temp;
		int test = 1;
		while(test < 100000000) {
			ArrayList<Double> predictErr = new ArrayList<Double>();
			test++;
			for(int j = 0; j < this.numberOfInstance; j++) {
				double h = 0;
				for(int i = 0; i < x.size(); i++) {
					if(i != index) {
						h += x.get(i).get(j)*w.get(i);
					}
				}
				temp = h - y.get(j);
				predictErr.add(temp);
			}
		
			// Modify w 
			for(int i = 0; i < this.w.size(); i ++) {
				temp = 0;
				for(int j = 0; j < numberOfInstance; j++) {
					temp += predictErr.get(j)*x.get(i).get(j);
				}
				System.out.print(temp + " ");
				w.set(i, w.get(i) - temp*learningRate/numberOfInstance);
			}
			System.out.println(w);
		}
	}
	
	public double predict(double[] data) {
		double predictVal = 0;
		if(data[0] != 1) {
			System.out.println("Index 0 must equal 1");
			return 0;
		}
		if(data.length != w.size()) {
			System.out.println("Data error");
			return 0;
		}
		for(int i = 0; i < data.length; i++) {
			predictVal += w.get(i)*data[i]; 
		}
		return predictVal;
	}
	
	// Feature scaling
	public void featureScaling() {
		
	}
	
	// invert matrix
	public static double[][] invert(double a[][]) 
    {
        int n = a.length;
        double x[][] = new double[n][n];
        double b[][] = new double[n][n];
        int index[] = new int[n];
        for (int i=0; i<n; ++i) 
            b[i][i] = 1;
 
        // Transform the matrix into an upper triangle
        gaussian(a, index);
 
        // Update the matrix b[i][j] with the ratios stored
        for (int i=0; i<n-1; ++i)
            for (int j=i+1; j<n; ++j)
                for (int k=0; k<n; ++k)
                    b[index[j]][k]
                    	    -= a[index[j]][i]*b[index[i]][k];
 
        // Perform backward substitutions
        for (int i=0; i<n; ++i) 
        {
            x[n-1][i] = b[index[n-1]][i]/a[index[n-1]][n-1];
            for (int j=n-2; j>=0; --j) 
            {
                x[j][i] = b[index[j]][i];
                for (int k=j+1; k<n; ++k) 
                {
                    x[j][i] -= a[index[j]][k]*x[k][i];
                }
                x[j][i] /= a[index[j]][j];
            }
        }
        return x;
    }
 
	// Method to carry out the partial-pivoting Gaussian
	// elimination.  Here index[] stores pivoting order.
 
    public static void gaussian(double a[][], int index[]) 
    {
        int n = index.length;
        double c[] = new double[n];
 
        // Initialize the index
        for (int i=0; i<n; ++i) 
            index[i] = i;
 
        // Find the rescaling factors, one from each row
        for (int i=0; i<n; ++i) 
        {
            double c1 = 0;
            for (int j=0; j<n; ++j) 
            {
                double c0 = Math.abs(a[i][j]);
                if (c0 > c1) c1 = c0;
            }
            c[i] = c1;
        }
 
        // Search the pivoting element from each column
        int k = 0;
        for (int j=0; j<n-1; ++j) 
        {
            double pi1 = 0;
            for (int i=j; i<n; ++i) 
            {
                double pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if (pi0 > pi1) 
                {
                    pi1 = pi0;
                    k = i;
                }
            }
 
            // Interchange rows according to the pivoting order
            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i=j+1; i<n; ++i) 	
            {
                double pj = a[index[i]][j]/a[index[j]][j];
 
                // Record pivoting ratios below the diagonal
                a[index[i]][j] = pj;
 
                // Modify other elements accordingly
                for (int l=j+1; l<n; ++l)
                    a[index[i]][l] -= pj*a[index[j]][l];
            }
        }
    }
	
	// Read x and y from specific file path
	private int readFile(String filePath, ArrayList<Instances> xTemp) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		int lineNum = 1, v;
		String line = br.readLine();
		String token[] = line.split(" ");
		if(!token[0].equals("@relation") ) {
			System.out.println("Missing relation declare");
		} else {
			this.name = token[1];
		}
		
		// Read attribute section
		line = br.readLine();
		line = br.readLine();
		token = line.split(" ");
		if(!token[0].equals("@attribute")) {
			System.out.println("Missing attribute declare");
		}
		while(token[0].equals("@attribute")) {
			Instances ins = new Instances(token[1]);
			xTemp.add(ins);
			line = br.readLine();
			token = line.split(" ");
		}
		line = br.readLine();
		token = line.split(" ");
		if(!token[0].equals("@data")) {
			System.out.println("Missing Data declare");
		}
		
		// Read data section
		int numberOfInstance = 0;
		while((line = br.readLine()) != null ) {
			numberOfInstance++;
			token = line.split(",");
			for(int i = 1; i < xTemp.size(); i++) {
				double d = Double.parseDouble(token[i-1]);
				xTemp.get(i).addData(d);
			}
		}
		return numberOfInstance;
	}
	
	public void evaluate(String testFilePath) {
		// Read test file
		ArrayList<Instances> xTest = new ArrayList<>();
		int num = 0;
		// add x0 to array
		Instances x0 = new Instances("x0");
		xTest.add(x0);
		
		try {
			num = readFile(testFilePath, xTest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Instances yTest = xTest.get(index);
		xTest.remove(index);
		
		// set x0's all elements to 1
		for(int i = 0; i < num; i ++) {
			xTest.get(0).addData(1);
		}
		
		// Calculate Mean Squared Error
		double mae = 0;
		double rmse = 0;
		for(int i = 0; i < num; i++) {
			double temp = 0;
			for(int j = 0; j < xTest.size(); j++) {
				temp = temp + xTest.get(j).get(i)*w.get(j);
			}
			mae += Math.abs(temp - yTest.get(i));
			rmse += Math.pow(temp - yTest.get(i), 2);
		}
		System.out.println("Mean absolute error: " + mae/num);
		System.out.println("Root mean squared error: " + Math.sqrt(rmse/num));
    }
	
	public int getNumberOfAttributes() {
		return this.x.size();
	}
	
	public int getNumberOfInstances() {
		return this.numberOfInstance;
	}
	
	public ArrayList<Instances> getVariable() {
		return this.x;
	}
	
	public ArrayList<Double> getW() {
		return this.w;
	}
	
	public void printX() {
		for(int i = 0; i < numberOfInstance; i ++) {
			for(int j = 0; j < x.size(); j++) {
				System.out.print(x.get(j).get(i) + " ");
			}
			System.out.println();
		}
	}
	
	public void printY() {
		System.out.println(y.getDatas());
	}
	
	public void printW() {
		System.out.println(w);
	}
	
	public void printFormular() {
		System.out.print("y = " + w.get(0));
		for(int i = 1; i < w.size(); i++) {
			System.out.print(" + " + w.get(i) + "*X" + i);
		}
		System.out.println();
	}
}

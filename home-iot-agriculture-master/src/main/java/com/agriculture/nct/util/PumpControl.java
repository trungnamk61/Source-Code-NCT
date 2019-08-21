package com.agriculture.nct.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.agriculture.nct.model.Command;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.LinguisticTerm;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class PumpControl {

    /** File chứa dữ liệu để trainning và test */
    private static final Resource TRAINING_DATA_SET_FILENAME = new ClassPathResource("data/ln_train_data_ec");
    private static final Resource TRAINING_PH_DATA_SET_FILENAME = new ClassPathResource("data/ln_train_data_ph");
    private static final Resource TESTING_DATA_SET_FILENAME = new ClassPathResource("data/ln_test_data_ec");
    private static final Resource TESTING_PH_DATA_SET_FILENAME = new ClassPathResource("data/ln_test_data_ph");
    private static final Resource FUZZY_PUMP_RULE = new ClassPathResource("lib/PumpRule.fcl");

    private LinearRegression ln = null;
    private LinearRegression lnPh = null;

    public PumpControl() {
        // Train model
        try {
            // Train for Ec
            ln = new LinearRegression(TRAINING_DATA_SET_FILENAME.getFile().getPath(), 3);
            ln.trainWithNormalEquation();
            ln.printFormular();
            ln.evaluate(TESTING_DATA_SET_FILENAME.getFile().getPath());
            // Train for Ph
            lnPh = new LinearRegression(TRAINING_PH_DATA_SET_FILENAME.getFile().getPath(), 3);
            lnPh.trainWithNormalEquation();
            lnPh.printFormular();
            lnPh.evaluate(TESTING_PH_DATA_SET_FILENAME.getFile().getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sử dụng Fuzzy kết hợp với linear regression để tìm ra số ml dung dịch A&B cần cho thêm
     */
    public ArrayList<Command> autoConrolPump(double curEc, double curpH, int curDay, double phmax, int dev_id, double phmin, double ecmax) {
        double ABSolAmount = 0, ECpridict, ecForToDay, pHpridict, pHDownAmount = 0;
        curEc = curEc*1000;
        ecmax = ecmax*1000;
        ArrayList<Command> lsCmd = new ArrayList<>();
        try {
            ABSolAmount = 0;
            ECpridict = predictEc(ABSolAmount, curEc);
            ecForToDay = getEcByDay(curDay);

//            if(curEc > ecmax || curpH < phmin) {
//                //lsCmd.add(new Command(dev_id, "DRAIN", "ON", 500, Command.FROM_SERVER));
//                //lsCmd.add(new Command(dev_id, "PUMP_WATER", "ON", 500, Command.FROM_SERVER));
//                return lsCmd;
//            }

            while (ECpridict < ecForToDay) {
                ABSolAmount = ABSolAmount + 0.5;
                ECpridict = predictEc(ABSolAmount, curEc);
            }

            pHDownAmount = 0;
            pHpridict = predictPhUseLinear(pHDownAmount, curpH);
            while (pHpridict >  phmax) {
                pHDownAmount = pHDownAmount + 0.5;
                pHpridict = predictPhUseLinear(pHDownAmount, curpH);
            }

            System.out.println("Ec of day " + curDay + ": " +  ecForToDay);
            System.out.println("ECcur: " + curEc + "  " + "PHcur: " + curpH);
            System.out.println("ABSolAmount: " + ABSolAmount);
            System.out.println("pHSolAmount: " + pHDownAmount);
            System.out.println("ECpridict: " + ECpridict);
            System.out.println("pHpridict: " + pHpridict);
        } catch (Exception e) {
            e.printStackTrace();
        }
        lsCmd.add(new Command(dev_id, "PUMP_A", "ON", (int) Math.round(ABSolAmount/2), Command.FROM_SERVER));
        lsCmd.add(new Command(dev_id, "PUMP_B", "ON", (int) Math.round(ABSolAmount/2), Command.FROM_SERVER));
        lsCmd.add(new Command(dev_id, "PUMP_PH_DOWN", "ON", (int) Math.round(pHDownAmount), Command.FROM_SERVER));
        return lsCmd;
    }

    public double getEcByDay(int day) {
        HashMap<String, Variable> variables;
        HashMap<String, LinguisticTerm> lterms;

        String filename = null;
        try {
            filename = FUZZY_PUMP_RULE.getFile().getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FIS fis = FIS.load(filename, true);
        if (fis == null) {
            System.err.println("Can't load file: '" + filename + "'");
            System.exit(1);
        }

        // Change default variable
        FunctionBlock fb = fis.getFunctionBlock(null);
        // Set inputs
        fb.setVariable("Date", day);
        fb.evaluate();
        Variable ecByday = fb.getVariable("EC");
        //JFuzzyChart.get().chart(fb);
        //JFuzzyChart.get().chart(ecByday, ecByday.getDefuzzifier(), true);
        return ecByday.getValue();
    }

    /**
     * Sử dụng linear regression để predict khoảng thay đổi EC, từ đó predict trạng thái dung dịch tiếp theo
     */
    private double predictEc(double solABQuan, double curEc) throws Exception {
        double[] data = new double[ln.getNumberOfAttributes()];
        data[0] = 1;
        data[1] = curEc;
        data[2] = solABQuan;
        return curEc + ln.predict(data);
    }

    /**
     * Sử dụng linear regression để predict PH sau khi điều chỉnh
     */
    private double predictPhUseLinear(double solPhDown, double curPH) throws Exception {
        double[] data = new double[lnPh.getNumberOfAttributes()];
        data[0] = 1;
        data[1] = solPhDown;
        data[2] = curPH;
        return lnPh.predict(data);
    }
}

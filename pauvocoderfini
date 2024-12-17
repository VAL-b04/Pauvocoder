


import static java.lang.System.exit;

public class Pauvocoder {

    // Processing SEQUENCE size (100 msec with 44100Hz samplerate)
    final static int SEQUENCE = StdAudio.SAMPLE_RATE/10;

    // Overlapping size (20 msec)
    final static int OVERLAP = SEQUENCE/5 ;
    // Best OVERLAP offset seeking window (15 msec)
    final static int SEEK_WINDOW = 3*OVERLAP/4;

    public static void main(String[] args) {
        if (args.length < 2)
        {
            System.out.println("usage: pauvocoder <input.wav> <freqScale>\n");
            exit(1);
        }



        String wavInFile = args[0];
        double freqScale = Double.valueOf(args[1]);
        String outPutFile= wavInFile.split("\\.")[0] + "_" + freqScale +"_";

        // Open input .wev file
        System.out.println("Opening " + wavInFile);
        double[] inputWav = StdAudio.read(wavInFile);

        for (int i = 0; i < 1000; i+=10){
            double sim = correlation(inputWav, 5000, 5000 + i + OVERLAP);
            System.out.println(i + "sim = " + sim);
        }
        exit(0);

        // Resample test
        System.out.println("Resampling");
        double[] newPitchWav = resample(inputWav, freqScale);
        StdAudio.save(outPutFile+"Resampled.wav", newPitchWav);

        // Simple dilatation
        System.out.println("Simple dilatation");
        double[] outputWav   = vocodeSimple(newPitchWav, 1.0/freqScale);
        StdAudio.save(outPutFile+"Simple.wav", outputWav);

        // Simple dilatation with overlaping
        System.out.println("Simple dilatation with overlaping");
        outputWav = vocodeSimpleOver(newPitchWav, 1.0/freqScale);
        StdAudio.save(outPutFile+"SimpleOver.wav", outputWav);

        // Simple dilatation with overlaping and maximum cross correlation search
        System.out.println("Simple dilatation with overlaping and maximum cross correlation search");
        outputWav = vocodeSimpleOverCross(newPitchWav, 1.0/freqScale);
        StdAudio.save(outPutFile+"SimpleOverCross.wav", outputWav);

        // joue(outputWav);

        // Some echo above all
        // outputWav = echo(outputWav, 100, 0.7);
        outputWav = echo(outputWav, 100, 0.7);
        StdAudio.save(outPutFile+"SimpleOverCrossEcho.wav", outputWav);

        // Display waveform
        displayWaveform(outputWav);

    }

    /**
     * Resample inputWav with freqScale
     * @param inputWav
     * @param freqScale
     * @return resampled wav
     */
    public static double[] resample(double[] inputWav, double freqScale) {
        double raison;
        double outputWav[];
        int taille = inputWav.length;
        int n = 0;
        if (freqScale > 1) {
            raison = (freqScale-1.0)/freqScale;
            taille = (int)(inputWav.length * (1 - raison) + 1);
        }
        else if (freqScale < 1) {
            raison = (1.0-freqScale)/freqScale;
            taille = (int)(inputWav.length * (raison + 1) + 1);
        }

        System.out.println("###########################");
        System.out.println("resample");
        System.out.println("freqScale = " + freqScale);
        System.out.println("old taille = " + inputWav.length);
        System.out.println("new taille = " + taille);

        outputWav = new double[taille];
        for (double i = 0; i < inputWav.length; i += freqScale) {
            outputWav[n++] = inputWav[(int)i];
        }
        System.out.println("n = " + n);
        return outputWav;
    }

    /**
     * Simple dilatation, without any overlapping
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimple(double[] inputWav, double dilatation) {
        double saut = (SEQUENCE * dilatation);
        int n = 0;
        double outputWav[];
        int taille = (int)(inputWav.length / dilatation) + 1;

        outputWav = new double[taille];

        System.out.println("###########################");
        System.out.println("vocodeSimple");
        System.out.println("dilatation = " + dilatation);
        System.out.println("saut = " + saut);
        System.out.println("SEQUENCE = " + SEQUENCE);
        System.out.println("old taille = " + inputWav.length);
        System.out.println("new taille = " + taille);

        for (double i = 0; i < inputWav.length; i += saut) {
            for (int j = 0; j < SEQUENCE; j++) {
                if ((int)(i+j) >= inputWav.length || n >= taille) {
                    break;
                }
                outputWav[n++] = inputWav[(int)(i+j)];
            }
        }
        System.out.println("n = " + n);
        return outputWav;
    }

    public static int IdontKnowHowToNameIt(double[] inputWav, double[] outputWav, int i, int seq, int n, int offset) {
        n -= OVERLAP;
        for (int j = 0; j < seq ; j++) {
            int index = i+j + offset;
            if (index >= inputWav.length || n >= outputWav.length)
                break;

            if (j < OVERLAP) {
                double coefficient = ((double)j / (double)OVERLAP);
                outputWav[n++] += inputWav[index] * coefficient;
            }
            else if (j >= OVERLAP && j < seq - OVERLAP) {
                outputWav[n++] = inputWav[index];
            }
            else {
                double coefficient = ((double)(seq - j) / (double)OVERLAP);
                outputWav[n++] = inputWav[index] * coefficient;
            }
        }
        return n;
    }

    /**
     * Simple dilatation, with overlapping
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOver(double[] inputWav, double dilatation) {
        int seq = SEQUENCE + OVERLAP;
        int saut = (int) (SEQUENCE * dilatation);
        int n = OVERLAP;
        double outputWav[];
        int taille = (int)(inputWav.length / dilatation) + 1;
        
        outputWav = new double[taille];

        System.out.println("###########################");
        System.out.println("vocodeSimpleOver");
        System.out.println("dilatation = " + dilatation);
        System.out.println("saut = " + saut);
        System.out.println("SEQUENCE = " + SEQUENCE);
        System.out.println("seq = " + seq);
        System.out.println("OVERLAP = " + OVERLAP);
        System.out.println("old taille = " + inputWav.length);
        System.out.println("new taille = " + outputWav.length);


        for (int i = 0; i < inputWav.length; i += saut) {
            n = IdontKnowHowToNameIt(inputWav, outputWav, i, seq, n, 0);
        }
        System.out.println("n = " + n);
        return outputWav;
    }

    public static double correlation(double[] inputWav, int decStart, int incStop) {
        double sim = 0;
        for (int i = 0; i < OVERLAP; i++) {
            if (decStart + i >= inputWav.length || incStop - i >= inputWav.length)
                break;
            sim += inputWav[decStart + i] * inputWav[incStop - i];
        }
        return sim;
    }

    public static int calculOffset(double[] inputWav, int decStart, int incStop) {  
        double similarity = correlation(inputWav, decStart, incStop);
        int offset = 0;
        for (int i = 1; i < SEEK_WINDOW; i++) {
            double sim = correlation(inputWav, decStart, incStop + i);
            if (Math.abs(sim) < similarity) {
                similarity = sim;
                offset = i;
            }
            // System.out.println(" offset = " + i);
            // System.out.println(" similarity = " + sim);
        }
        return offset;
    }

    /**
     * Simple dilatation, with overlapping and maximum cross correlation search
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOverCross(double[] inputWav, double dilatation) {
        int seq = SEQUENCE + OVERLAP;
        int saut = (int) (SEQUENCE * dilatation);
        int n = OVERLAP;
        double outputWav[];
        int taille = (int)(inputWav.length / dilatation) + 1;
        
        outputWav = new double[taille];

        System.out.println("###########################");
        System.out.println("vocodeSimpleOverCross");
        System.out.println("dilatation = " + dilatation);
        System.out.println("saut = " + saut);
        System.out.println("SEQUENCE = " + SEQUENCE);
        System.out.println("seq = " + seq);
        System.out.println("OVERLAP = " + OVERLAP);
        System.out.println("SEEK_WINDOW = " + SEEK_WINDOW);
        System.out.println("old taille = " + inputWav.length);
        System.out.println("new taille = " + outputWav.length);


        int offset = 0;
        for (int i = 0; i < inputWav.length; i += saut) {
            System.out.println("offset = " + offset);
            n = IdontKnowHowToNameIt(inputWav, outputWav, i, seq, n, offset);
            offset = calculOffset(inputWav, i+seq+offset-OVERLAP, i+saut+OVERLAP);
        }
        System.out.println("n = " + n);
        return outputWav;
    }
    

    /**
     * Play the wav
     * @param wav
     */
    public static void joue(double[] wav) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Add an echo to the wav
     * @param wav
     * @param delayMs in msec
     * @param attn
     * @return wav with echo
     */
    public static double[] echo(double[] wav, double delayMs, double attn) {
        for(int index = 0; index < wav.length; index++) {
            int new_index = index - (int)(delayMs * StdAudio.SAMPLE_RATE / 1000);
            if (new_index >= 0) {
                wav[index] += wav[new_index] * attn;
                if (wav[index] > 1.0) {wav[index] = 1.0;}
                if (wav[index] < -1.0) {wav[index] = -1.0;}
            }
        }
        return wav;
    }

    /**
     * Display the waveform
     * @param wav
     */
    public static void displayWaveform(double[] wav) {
        throw new UnsupportedOperationException("Not implemented yet");
    }


}

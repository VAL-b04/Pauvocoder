
public class Pauvocoder
{
    // Processing SEQUENCE size (100 msec with 44100Hz samplerate)
    final static int SEQUENCE = StdAudio.SAMPLE_RATE/10;

    // Overlapping size (20 msec)
    final static int OVERLAP = SEQUENCE/5 ;

    // Best OVERLAP offset seeking window (15 msec)
    final static int SEEK_WINDOW = 3*OVERLAP/4;

    /**
     * Resample inputWav with freqScale
     * @param inputWav
     * @param freqScale
     * @return resampled wav
     */
    public static double[] resample(double[] inputWav, double freqScale)
    {
        double raison; // Ratio utilisé pour calculer la nouvelle taille du tableau rééchantillonné
        double outputWav[];
        int taille = inputWav.length;
        int n = 0;
        
        if (freqScale > 1) // Sous-échantillonnage
        {
            raison = (freqScale - 1.0) / freqScale; // Calcul du ratio à utiliser pour réduire la taille
            taille = (int)(inputWav.length * (1 - raison) + 1);
        }
        else if (freqScale < 1)  // Sur-échantillonnage
        {
            raison = (1.0 - freqScale) / freqScale;
            taille = (int)(inputWav.length * (raison + 1) + 1);
        }

        System.out.println("###########################");
        System.out.println("resample");
        System.out.println("freqScale = " + freqScale);
        System.out.println("old taille = " + inputWav.length);
        System.out.println("new taille = " + taille);

        outputWav = new double[taille]; // Initialisation du tableau de sortie avec la nouvelle taille

        for (double i = 0; i < inputWav.length; i += freqScale) // Parcourt du tableau d'entrée avec un pas défini par freqScale
        {
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
    public static double[] vocodeSimple(double[] inputWav, double dilatation)
    {
        double saut = (SEQUENCE * dilatation); // Pas entre chaque segment à traiter en fonction de la dilatation
        int n = 0;
        double outputWav[]; // Tableau pour contenir le résultat du traitement
        int taille = (int)(inputWav.length / dilatation) + 1; // Calcul de la taille du tableau de sortie

        outputWav = new double[taille];

        System.out.println("###########################");
        System.out.println("vocodeSimple");
        System.out.println("dilatation = " + dilatation);
        System.out.println("saut = " + saut);
        System.out.println("SEQUENCE = " + SEQUENCE);
        System.out.println("old taille = " + inputWav.length);
        System.out.println("new taille = " + taille);

        for (double i = 0; i < inputWav.length; i += saut) // Parcourt le tableau d'entrée avec un pas défini par saut
        {
            for (int j = 0; j < SEQUENCE; j++) // Copie les échantillons du tableau d'entrée vers le tableau de sortie
            {
                if ((int)(i+j) >= inputWav.length || n >= taille)  // Si les indices dépassent les limites des tableaux on arrête
                {
                    break;
                }
                outputWav[n++] = inputWav[(int)(i+j)];
            }
        }
        System.out.println("n = " + n);
        return outputWav;
    }

    /**
     *
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static int applyOverlapAndCrossfade(double[] inputWav, double[] outputWav, int i, int seq, int n, int offset)
    {
        n -= OVERLAP;

        for (int j = 0; j < seq ; j++) // Boucle qui traite chaque échantillon de la séquence
        {
            int index = i+j + offset; // Calcul de l'indice du tableau d'entrée

            if (index >= inputWav.length || n >= outputWav.length) // Si l'indice dépasse les limites des tableaux on arrête
            {
                break;
            }

            if (j < OVERLAP)  // Applique un coefficient progressif pour lisser les transitions dans la zone de chevauchement
            {
                double coefficient = ((double)j / (double)OVERLAP); // Coefficient de montée
                outputWav[n++] += inputWav[index] * coefficient;
            }
            else if (j >= OVERLAP && j < seq - OVERLAP) // Copie les échantillons dans la zone sans chevauchement
            {
                outputWav[n++] = inputWav[index];
            }
            else // Applique un coefficient progressif pour réduire la transition
            {
                double coefficient = ((double)(seq - j) / (double)OVERLAP); // Coefficient de descente
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
    public static double[] vocodeSimpleOver(double[] inputWav, double dilatation)
    {
        int seq = SEQUENCE + OVERLAP; // Longueur de chaque segment à traiter
        int saut = (int)(SEQUENCE * dilatation);
        int n = OVERLAP; // Début d'écriture dans le tableau de sortie
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

        for (int i = 0; i < inputWav.length; i += saut) // Parcours du tableau d'entrée avec un pas défini par saut
        {
            n = applyOverlapAndCrossfade(inputWav, outputWav, i, seq, n, 0); // Appel à la méthode applyOverlapAndCrossfade pour traiter et copier un segment
        }
        System.out.println("n = " + n);
        return outputWav;
    }

    /**
     * Calculates the correlation
     * @param inputWav
     * @param decStart
     * @param decincStopStart
     * @return sim
     */
    public static double correlation(double[] inputWav, int decStart, int incStop)
    {
        double sim = 0; // Variable pour accumuler la corrélation

        for (int i = 0; i < OVERLAP; i++) // Parcours des échantillons sur la zone de chevauchement
        {
            if (decStart + i >= inputWav.length || incStop - i >= inputWav.length) // Vérifie que les indices restent dans les limites du tableau
            {
                break;
            }
            sim += inputWav[decStart + i] * inputWav[incStop - i];
        }
        return sim;
    }

    /**
     * Calculates the offset
     * @param inputWav
     * @param decStart
     * @param incStop
     * @return offset
     */
    public static int calculOffset(double[] inputWav, int decStart, int incStop)
    {
        double similarity = correlation(inputWav, decStart, incStop); // Calcul initial de la corrélation sans décalage
        int offset = 0;

        for (int i = 1; i < SEEK_WINDOW; i++) // Parcourt les décalages possibles dans une fenêtre de recherche définie par SEEK_WINDOW
        {
            double sim = correlation(inputWav, decStart, incStop + i); // Calcule la corrélation

            if (Math.abs(sim) < similarity)
            {
                similarity = sim;
                offset = i;
            }
        }
        return offset;
    }

    /**
     * Simple dilatation, with overlapping and maximum cross correlation search
     * @param inputWav
     * @param dilatation factor
     * @return dilated wav
     */
    public static double[] vocodeSimpleOverCross(double[] inputWav, double dilatation)
    {
        int seq = SEQUENCE + OVERLAP; // Taille d'un segment incluant le recouvrement
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

        for (int i = 0; i < inputWav.length; i += saut) // Parcourt les segments de l'entrée en sautant le nombre d'échantillons déterminé par saut
        {
            n = applyOverlapAndCrossfade(inputWav, outputWav, i, seq, n, offset); // Applique le recouvrement et le crossfade au segment
            offset = calculOffset(inputWav, i+seq+offset-OVERLAP, i+saut+OVERLAP); // Calcule de l'offset pour aligner le prochain segment
        }
        System.out.println("n = " + n);
        return outputWav;
    }

    /**
     * Play the wav
     * @param wav
     */
    public static void joue(double[] wav)
    {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Add an echo to the wav
     * @param wav
     * @param delayMs in msec
     * @param attn
     * @return wav with echo
     */
    public static double[] echo(double[] wav, double delayMs, double attn)
    {
        for(int index = 0; index < wav.length; index++) // Parcourt chaque échantillon du tableau
        {
            int new_index = index - (int)(delayMs * StdAudio.SAMPLE_RATE / 1000); // Calcule l'indice correspondant à l'écho avec le délai spécifié
            
            if (new_index >= 0) // Vérifie si l'indice décalé est valide
            {
                wav[index] += wav[new_index] * attn; // Ajoute la valeur atténuée du signal décalé à l'échantillon
                if (wav[index] > 1.0)
                {
                    wav[index] = 1.0;
                }
                if (wav[index] < -1.0)
                {
                    wav[index] = -1.0;
                }
            }
        }
        return wav;
    }

    /**
     * Display the waveform
     * @param wav
     */
    public static void displayWaveform(double[] wav)
    {
        throw new UnsupportedOperationException("Not implemented yet");
    }







    // Fonction pour tracer une forme d'onde audio (signal)
    public static void plotWaveform(double[] wav, int width, int height, int step) {
        // Nombre d'échantillons à afficher
        int n = wav.length;

        // Calcul du pas (système de coordonnées pour dessiner l'audio)
        double stepSize = (double) width / n;
        double scale = height / 2.0; // Pour que l'amplitude soit entre -1 et 1 dans l'affichage

        for (int i = 0; i < n - 1; i++) {
            double x1 = i * stepSize;
            double y1 = wav[i] * scale;

            double x2 = (i + 1) * stepSize;
            double y2 = wav[i + 1] * scale;

            // Tracer la ligne entre les deux points successifs (forme d'onde)
            StdDraw.line(x1, y1, x2, y2);
        }
    }

    public static void displayAudioProcess(double[] inputWav, double[] outputWav) {
        // Configuration de la fenêtre pour afficher les signaux
        int width = 800;  // Largeur de la fenêtre d'affichage
        int height = 400; // Hauteur de la fenêtre d'affichage
        StdDraw.setCanvasSize(width, height);
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(-1.2, 1.2);

        // Affichage de l'audio original
        StdDraw.setPenColor(StdDraw.RED); // Couleur rouge pour le signal original
        StdDraw.text(width / 2, 1.1, "Original Audio");
        plotWaveform(inputWav, width, height, 0);

        // Affichage de l'audio après transformation finale (vocode ou autre)
        StdDraw.setPenColor(StdDraw.GREEN); // Couleur verte pour l'audio après transformation
        StdDraw.text(width / 2, 0.5, "Processed Audio");
        plotWaveform(outputWav, width, height, 2);

        // Attente pour que l'utilisateur puisse voir les résultats
        StdDraw.show();
    }



    public static void main(String[] args) {
        // Vérification des arguments d'entrée
        if (args.length < 2) {
            System.out.println("Usage: pauvocoder <input.wav> <freqScale>");
            System.exit(1);
        }

        String wavInFile = args[0];   // Fichier d'entrée
        double freqScale = Double.valueOf(args[1]); // Facteur de changement de fréquence
        String outPutFile = wavInFile.split("\\.")[0] + "_" + freqScale + "_"; // Génère le nom du fichier de sortie

        // Chargement du fichier audio d'entrée
        System.out.println("Ouverture du fichier : " + wavInFile);
        double[] inputWav = StdAudio.read(wavInFile);

        // Redressement du pitch (modification de la fréquence)
        System.out.println("Rééchantillonnage...");
        double[] resampledWav = resample(inputWav, freqScale);
        StdAudio.save(outPutFile + "Resampled.wav", resampledWav);

        // Appliquer une dilatation simple sans chevauchement
        System.out.println("Application de la dilatation simple...");
        double[] outputWav = vocodeSimple(resampledWav, 1.0 / freqScale);
        StdAudio.save(outPutFile + "Simple.wav", outputWav);

        // Appliquer une dilatation simple avec chevauchement
        System.out.println("Application de la dilatation simple avec chevauchement...");
        outputWav = vocodeSimpleOver(resampledWav, 1.0 / freqScale);
        StdAudio.save(outPutFile + "SimpleOver.wav", outputWav);

        // Appliquer une dilatation simple avec chevauchement et recherche de corrélation croisée maximale
        System.out.println("Application de la dilatation avec chevauchement et recherche de corrélation croisée...");
        outputWav = vocodeSimpleOverCross(resampledWav, 1.0 / freqScale);
        StdAudio.save(outPutFile + "SimpleOverCross.wav", outputWav);

        // Jouer le fichier traité
        //joue(outputWav);

        // Ajouter un écho
        System.out.println("Ajout d'un écho...");
        outputWav = echo(outputWav, 100, 0.7);
        StdAudio.save(outPutFile + "SimpleOverCrossEcho.wav", outputWav);

        // Affichage de la forme d'onde (si vous avez implémenté le graphique avec StdDraw)
        displayAudioProcess(inputWav, outputWav);
    }
}

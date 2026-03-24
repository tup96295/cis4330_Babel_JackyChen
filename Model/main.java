public class Main {
    public static void main(String[] args) {

        BabelModel model = new BabelModel(256);

        model.registerModality("vision", new VisionEncoder(512));
        model.registerModality("audio", new AudioEncoder(512));

        float[][][] image = new float[224][224][3];
        double[] audio = new double[128];

        double[] visionEmbedding = model.forward("vision", image);
        double[] audioEmbedding = model.forward("audio", audio);

        System.out.println("Vision embedding size: " + visionEmbedding.length);
        System.out.println("Audio embedding size: " + audioEmbedding.length);
    }
}

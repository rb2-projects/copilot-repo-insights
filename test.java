import java.io.BufferedReader;
import java.io.InputStreamReader;

public class test {
    public static void main(String[] args) throws Exception {
        // Test Copilot CLI directly
        ProcessBuilder pb = new ProcessBuilder("copilot", "-p", "What is 2+2?", "-s");
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
            System.out.println(">>> " + line);
        }
        
        int exitCode = process.waitFor();
        System.out.println("Exit code: " + exitCode);
        System.out.println("Output: " + output.toString());
    }
}

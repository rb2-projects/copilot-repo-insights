# Test script for AI-enhanced analysis
Write-Host "Testing baseline (no AI flag)..."
mvn -q exec:java

Write-Host "`nTesting with --enable-ai flag..."
java -cp target/classes com.rb.repoinsight.Main --enable-ai

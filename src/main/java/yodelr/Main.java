package yodelr;

import java.io.DataInputStream;
import java.io.IOException;

import static java.lang.System.out;
import static yodelr.ConsoleUtils.showHelp;

public class Main {

  public static void main(String[] args) {

    var api = Yodelr.make();

    showHelp();

    try {
      var in = new DataInputStream(System.in);
      Cli.loop(in, api);
    } catch (IOException e) {
      out.println("Something went wrong with standard input. Exiting.");
    }
  }
}
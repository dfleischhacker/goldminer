package miner.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssociationRulesParser {

    public AssociationRulesParser() {
    }

    public List<ParsedAxiom> parse(File rules, boolean secondAnte) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(rules));
        String patternRegex;
        if (secondAnte) {
            patternRegex = "^(\\d+)\\s+<-\\s+(\\d+)\\s+(\\d+)\\s+\\((\\d+(?:\\.\\d+)?),\\s+(\\d+(?:\\.\\d+)?(?:e[+-]\\d+)?)\\)$";
        }
        else {
            patternRegex = "^(\\d+)\\s+<-\\s+(\\d+)\\s+\\((\\d+(?:\\.\\d+)?),\\s+(\\d+(?:\\.\\d+)?(?:e[+-]\\d+)?)\\)$";
        }
        String line;
        Pattern pattern = Pattern.compile(patternRegex);
        List<ParsedAxiom> axioms = new ArrayList<ParsedAxiom>();
        while ((line = in.readLine()) != null) {
            Matcher matcher = pattern.matcher(line.trim());
            boolean matches = matcher.matches();
            if (!matches || matcher.groupCount() != 4) {
                System.out.println("Unable to parse: '" + line + "'");
                System.out.println("Matches: " + matches);
                if (matches)
                    System.out.println("Group Count: " + matcher.groupCount());
                continue;
            }
            int cons = Integer.parseInt(matcher.group(1));
            int ante = Integer.parseInt(matcher.group(2));

            int counter = 3;
            int ante2 = -1;
            if (secondAnte) {
                ante2 = Integer.parseInt(matcher.group(counter++));
            }
            double supp = Double.parseDouble(matcher.group(counter++));
            double conf = Double.parseDouble(matcher.group(counter));
            if (secondAnte) {
                axioms.add(new ParsedAxiom(ante, ante2, cons, supp, conf));
            }
            else {
                axioms.add(new ParsedAxiom(ante, cons, supp, conf));
            }
        }

        return axioms;
    }
}

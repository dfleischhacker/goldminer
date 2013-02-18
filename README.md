# GOLD Miner


## About
GOLD Miner is a tool for Statistical Schema Induction as proposed by Völker and Niepert [1] and extended by
Fleischhacker and Völker [2][3].

[1] Johanna Völker and Mathias Niepert. Statistical Schema Induction. The Semantic Web: Research and Applications : 8th Extended Semantic Web Conference, ESWC 2011, Heraklion, Crete, Greece, Proceedings, Part I, 2011.

[2] Daniel Fleischhacker and Johanna Völker. Inductive Learning of Disjointness Axioms. On the Move to Meaningful Internet Systems: OTM 2011 : Confederated International Conferences: CoopIS, DOA-SVI, and ODBASE 2011, Hersonissos, Crete, Greece, Proceedings, Part II, 2011.

[3] Daniel Fleischhacker, Johanna Völker and Heiner Stuckenschmidt. Mining RDF Data for Property Axioms. On the Move to Meaningful Internet Systems: OTM 2012 : Confederated International Conferences: CoopIS, DOA-SVI, and ODBASE 2012. Proceedings, Part II, 2012.

## Usage

### Building
If you downloaded the source source of Gold Miner, you have to build the binary distribution first. The build process
is based on Apache Maven (http://maven.apache.org). After having installed Maven, change into the Gold Miner source
directory and use "mvn package" to start the build process. Have a look at the target/ folder which contains the
gold-miner-VERSION-jar-with-dependencies.jar which is the one you would like to use most probably.

### Remark
In the following we use
        goldminer.jar
to refer to the name of the Gold Miner JAR. For calling Gold Miner, you have to replace goldminer.jar by the correct
file name or rename your jar file to goldminer.jar.

### Configuring Gold Miner
Call gold miner using the genconfig command
    java -jar goldminer.jar genconfig
which generates the config files miner.properties and axioms.properties in the current directory.
Adapt miner.properties to your environment and choose the axiom types you want to generate by setting them to true
in axioms.properties. The axioms types not to generate have to be set to false.

### Running Gold Miner
Running Gold Miner includes three phases. In the first phase, Gold Miner inspects the ontology for concepts and
properties, the SPARQL endpoint for instances and generates the transaction tables required for the currently
configured axiom types. This phase might take longer depending on the performance of your SPARQL endpoint. You can
start the generation phase using
    java -jar goldminer.jar generate

The next step uses the Borgelt apriori miner to generate association rules and is started by
    java -jar goldminer.jar mine

In the final step, the association rules are parsed and the ontology is enriched with the resulting axioms. Use
    java -jar goldminer.jar parse
to start this phase.

### Re-running Gold Miner
If you want to re-run Gold Miner completely you either have to empty all directories which contain results from previous
runs or empty the checkpoints/ directory in your transaction_tables directory (as defined in the miner.properties config
file) since Gold Miner uses this directory to store its progress.

## License
Copyright (C) 2011-2012 Johanna Völker, Mathias Niepert, Daniel Fleischhacker

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

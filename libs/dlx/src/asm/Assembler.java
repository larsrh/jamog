
package asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Assembler
{
	public static final void main(String[] args)
	{
		// Get asm from whereever
		//String source = "/* test /* c */ */ #def VALUE {80000} #def BLA2 #def IF (TEST INS) {add r0 r0 r0 add r0 r0 r0 beqz TEST __AUTO__[1] add r0 r0 r0 INS __AUTO__[1]: } #ifndef BLA {baddla: addi r3 r0 VALUE} sgti r5 r3 500 IF{r5}{lb r2 VALUE(r3)} // bla blub";
		String source = " " + Preprocessor.STD_MAKROS + "IF r2 <= r5 {lb r6 100(r6)} ";

		ErrorHandler eh = new ErrorHandler(source);

		List<Token> tokens = new ArrayList<Token>();
		Map<String, Integer> labels = new HashMap<String, Integer>();
		List<Instruction> instructions = new ArrayList<Instruction>();
		List<Integer> opcodes = new ArrayList<Integer>();

		try
		{
			String asm = Preprocessor.process(source, eh);

			System.out.println(">>>" + asm + "<<<");

			Scanner.scan(asm, tokens, labels, eh);

			System.out.println("Tokens:");
			for(Token t : tokens)
				System.out.println(t);
			System.out.println("Labels:");
			for(Map.Entry<String, Integer> e : labels.entrySet())
				System.out.println(e.getKey() + ": " + e.getValue());

			Parser.parse(tokens, labels, instructions, eh);
		}
		catch(AssemblerException ae)
		{
			System.err.print("ERROR: " + ae.reason + "\n" + ae.place);
			System.exit(1);
		}

		for(Instruction i : instructions)
			opcodes.add(CodeGenerator.generate(i));

		// Write opcodes to whereever

		if(eh.warnings.size() != 0)
		{
			System.out.println("Warnings:");
			for(String[] s : eh.warnings)
				System.out.print("WARNING: " + s[0] + "\n" + s[1]);
		}

		System.out.println("Opcodes:");
		for(Integer i : opcodes)
			System.out.println("\t" + String.format("%08X", i));
	}
}

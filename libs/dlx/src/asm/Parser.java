
package asm;

import java.util.List;
import java.util.Map;

public final class Parser
{
	public static final void parse(List<Token> tokens, Map<String, Integer> labels, List<Instruction> instructions, ErrorHandler eh) throws AssemblerException
	{
		boolean[] register_use = new boolean[32];
		register_use[0] = true;
		for(int i = 1; i < 32; ++i)
			register_use[i] = false;

		for(int i = 0; i < tokens.size(); ++i)
		{
			Token t = tokens.get(i);
			Token p1, p2, p3;

			if(!(t instanceof Token.Mnemonic))
				throw new AssemblerException("Expected Instruction", eh.forParser(t));

			switch(((Token.Mnemonic)t).name)
			{
			case LB:
			case LBU:
			case LH:
			case LHU:
			case LW:
				if(tokens.size() - i - 1 < 2)
					throw new AssemblerException("Expected 2 parameters", eh.forParser(t));

				p1 = tokens.get(++i);
				p2 = tokens.get(++i);

				if(!(p1 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p1));
				if(!(p2 instanceof Token.Address))
					throw new AssemblerException("Expected address", eh.forParser(p2));

				if(!register_use[((Token.Address)p2).register_number])
					eh.addWarning("Uninitialized register", eh.forParser(p2));

				if(((Token.Address)p2).displacement < 0 || ((Token.Address)p2).displacement > 65535)
					eh.addWarning("Offset out of unsigned 16 bit range", eh.forParser(p2));

				if(instructions.size() >= 1 && checkRegisterLoad(instructions.get(instructions.size() - 1), ((Token.Address)p2).register_number))
					eh.addWarning("Used old register value", eh.forParser(p2));

				register_use[((Token.Register)p1).number] = true;
				instructions.add(new Instruction((Token.Mnemonic)t, p1, p2));
				break;
			case SB:
			case SH:
			case SW:
				if(tokens.size() - i - 1 < 2)
					throw new AssemblerException("Expected 2 parameters", eh.forParser(t));

				p1 = tokens.get(++i);
				p2 = tokens.get(++i);

				if(!(p1 instanceof Token.Address))
					throw new AssemblerException("Expected address", eh.forParser(p1));
				if(!(p2 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p2));

				if(!register_use[((Token.Address)p1).register_number])
					eh.addWarning("Uninitialized register", eh.forParser(p1));
				if(!register_use[((Token.Register)p2).number])
					eh.addWarning("Uninitialized register", eh.forParser(p2));

				if(((Token.Address)p1).displacement < 0 || ((Token.Address)p1).displacement > 65535)
					eh.addWarning("Offset out of unsigned 16 bit range", eh.forParser(p2));

				if(instructions.size() >= 1)
				{
					if(checkRegisterLoad(instructions.get(instructions.size() - 1), ((Token.Address)p1).register_number))
						eh.addWarning("Used old register value", eh.forParser(p1));
					if(checkRegisterLoad(instructions.get(instructions.size() - 1), ((Token.Register)p2).number))
						eh.addWarning("Used old register value", eh.forParser(p2));
				}

				instructions.add(new Instruction((Token.Mnemonic)t, p1, p2));
				break;
			case ADD:
			case SUB:
			case MULT:
			case MULTU:
			case DIV:
			case DIVU:
			case AND:
			case OR:
			case XOR:
			case SLL:
			case SRL:
			case SRA:
			case SLT:
			case SGT:
			case SLE:
			case SGE:
			case SEQ:
			case SNE:
			case FADD:
			case FSUB:
			case FMULT:
			case FDIV:
			case FSLT:
			case FSGT:
			case FSLE:
			case FSGE:
			case FSEQ:
			case FSNE:
				if(tokens.size() - i - 1 < 3)
					throw new AssemblerException("Expected 3 parameters", eh.forParser(t));

				p1 = tokens.get(++i);
				p2 = tokens.get(++i);
				p3 = tokens.get(++i);

				if(!(p1 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p1));
				if(!(p2 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p2));
				if(!(p3 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p3));

				if(!register_use[((Token.Register)p2).number])
					eh.addWarning("Uninitialized register", eh.forParser(p2));
				if(!register_use[((Token.Register)p3).number])
					eh.addWarning("Uninitialized register", eh.forParser(p3));

				if(instructions.size() >= 1)
				{
					if(checkRegisterLoad(instructions.get(instructions.size() - 1), ((Token.Register)p2).number))
						eh.addWarning("Used old register value", eh.forParser(p2));
					if(checkRegisterLoad(instructions.get(instructions.size() - 1), ((Token.Register)p3).number))
						eh.addWarning("Used old register value", eh.forParser(p3));
				}

				register_use[((Token.Register)p1).number] = true;
				instructions.add(new Instruction((Token.Mnemonic)t, p1, p2, p3));
				break;
			case FSINF:
			case FCITF:
			case FCFTI:
				if(tokens.size() - i - 1 < 2)
					throw new AssemblerException("Expected 2 parameters", eh.forParser(t));

				p1 = tokens.get(++i);
				p2 = tokens.get(++i);

				if(!(p1 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p1));
				if(!(p2 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p2));

				if(!register_use[((Token.Register)p2).number])
					eh.addWarning("Uninitialized register", eh.forParser(p2));

				if(instructions.size() >= 1 && checkRegisterLoad(instructions.get(instructions.size() - 1), ((Token.Register)p2).number))
					eh.addWarning("Used old register value", eh.forParser(p2));

				register_use[((Token.Register)p1).number] = true;
				instructions.add(new Instruction((Token.Mnemonic)t, p1, p2));
				break;
			case ADDI:
			case SUBI:
			case SLLI:
			case SRLI:
			case SRAI:
			case SLTI:
			case SGTI:
			case SLEI:
			case SGEI:
			case SEQI:
			case SNEI:
				if(tokens.size() - i - 1 < 3)
					throw new AssemblerException("Expected 3 parameters", eh.forParser(t));

				p1 = tokens.get(++i);
				p2 = tokens.get(++i);
				p3 = tokens.get(++i);

				if(!(p1 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p1));
				if(!(p2 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p2));
				if(!(p3 instanceof Token.Immediate))
					throw new AssemblerException("Expected immediate", eh.forParser(p3));

				if(!register_use[((Token.Register)p2).number])
					eh.addWarning("Uninitialized register", eh.forParser(p2));

				if(((Token.Immediate)p3).value < -32768 || ((Token.Immediate)p3).value > 32767)
					eh.addWarning("Immediate out of signed 16 bit range", eh.forParser(p3));

				if(instructions.size() >= 1 && checkRegisterLoad(instructions.get(instructions.size() - 1), ((Token.Register)p2).number))
					eh.addWarning("Used old register value", eh.forParser(p2));

				register_use[((Token.Register)p1).number] = true;
				instructions.add(new Instruction((Token.Mnemonic)t, p1, p2, p3));
				break;
			case ADDUI:
			case SUBUI:
			case ANDI:
			case ORI:
			case XORI:
				if(tokens.size() - i - 1 < 3)
					throw new AssemblerException("Expected 3 parameters", eh.forParser(t));

				p1 = tokens.get(++i);
				p2 = tokens.get(++i);
				p3 = tokens.get(++i);

				if(!(p1 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p1));
				if(!(p2 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p2));
				if(!(p3 instanceof Token.Immediate))
					throw new AssemblerException("Expected immediate", eh.forParser(p3));

				if(!register_use[((Token.Register)p2).number])
					eh.addWarning("Uninitialized register", eh.forParser(p2));

				if(((Token.Immediate)p3).value < 0 || ((Token.Immediate)p3).value > 65535)
					eh.addWarning("Immediate out of unsigned 16 bit range", eh.forParser(p3));

				if(instructions.size() >= 1 && checkRegisterLoad(instructions.get(instructions.size() - 1), ((Token.Register)p2).number))
					eh.addWarning("Used old register value", eh.forParser(p2));

				register_use[((Token.Register)p1).number] = true;
				instructions.add(new Instruction((Token.Mnemonic)t, p1, p2, p3));
				break;
			case LHI:
				if(tokens.size() - i - 1 < 2)
					throw new AssemblerException("Expected 2 parameters", eh.forParser(t));

				p1 = tokens.get(++i);
				p2 = tokens.get(++i);

				if(!(p1 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p1));
				if(!(p2 instanceof Token.Immediate))
					throw new AssemblerException("Expected immediate", eh.forParser(p2));

				if(((Token.Immediate)p2).value < 0 || ((Token.Immediate)p2).value > 65535)
					eh.addWarning("Immediate out of unsigned 16 bit range", eh.forParser(p2));

				register_use[((Token.Register)p1).number] = true;
				instructions.add(new Instruction((Token.Mnemonic)t, p1, p2));
				break;
			case BEQZ:
			case BNEZ:
				if(tokens.size() - i - 1 < 2)
					throw new AssemblerException("Expected 2 parameters", eh.forParser(t));

				p1 = tokens.get(++i);
				p2 = tokens.get(++i);

				if(!(p1 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p1));
				if(!(p2 instanceof Token.Immediate) && !(p2 instanceof Token.Reference))
					throw new AssemblerException("Expected immediate or label name", eh.forParser(p2));

				if(p2 instanceof Token.Immediate)
				{
					if(!register_use[((Token.Register)p1).number])
						eh.addWarning("Uninitialized register", eh.forParser(p1));

					if((instructions.size() >= 1 && checkRegisterWrite(instructions.get(instructions.size() - 1), ((Token.Register)p1).number)) || (instructions.size() >= 2 && checkRegisterWrite(instructions.get(instructions.size() - 2), ((Token.Register)p1).number)))
						eh.addWarning("Used old register value", eh.forParser(p1));

					if(((Token.Immediate)p2).value < -32768 || ((Token.Immediate)p2).value > 32767)
						eh.addWarning("Immediate out of signed 16 bit range", eh.forParser(p2));

					instructions.add(new Instruction((Token.Mnemonic)t, p1, p2));
				}
				else
				{
					Integer imm = labels.get(((Token.Reference)p2).label);

					if(imm == null)
						throw new AssemblerException("Label not defined", eh.forParser(p2));

					if(!register_use[((Token.Register)p1).number])
						eh.addWarning("Uninitialized register", eh.forParser(p1));

					if((instructions.size() >= 1 && checkRegisterWrite(instructions.get(instructions.size() - 1), ((Token.Register)p1).number)) || (instructions.size() >= 2 && checkRegisterWrite(instructions.get(instructions.size() - 2), ((Token.Register)p1).number)))
						eh.addWarning("Used old register value", eh.forParser(p1));

					imm = imm - instructions.size();
					if(imm < -32768 || imm > 32767)
						eh.addWarning("Label out of signed 16 bit range", eh.forParser(p2));

					instructions.add(new Instruction((Token.Mnemonic)t, p1, new Token.Immediate(imm)));
				}
				break;
			case J:
				if(tokens.size() - i - 1 < 1)
					throw new AssemblerException("Expected 1 parameters", eh.forParser(t));

				p1 = tokens.get(++i);

				if(!(p1 instanceof Token.Immediate) && !(p1 instanceof Token.Reference))
					throw new AssemblerException("Expected immediate or label name", eh.forParser(p1));

				if(p1 instanceof Token.Immediate)
				{
					if(((Token.Immediate)p1).value < -33554432 || ((Token.Immediate)p1).value > 33554431)
						eh.addWarning("Immediate out of signed 26 bit range", eh.forParser(p1));

					instructions.add(new Instruction((Token.Mnemonic)t, p1));
				}
				else
				{
					Integer imm = labels.get(((Token.Reference)p1).label);

					if(imm == null)
						throw new AssemblerException("Label not defined", eh.forParser(p1));

					imm = imm - instructions.size();
					if(imm < -33554432 || imm > 33554431)
						eh.addWarning("Label out of signed 26 bit range", eh.forParser(p1));

					instructions.add(new Instruction((Token.Mnemonic)t, new Token.Immediate(imm)));
				}
				break;
			case JR:
				if(tokens.size() - i - 1 < 1)
					throw new AssemblerException("Expected 1 parameters", eh.forParser(t));

				p1 = tokens.get(++i);

				if(!(p1 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p1));

				if(!register_use[((Token.Register)p1).number])
					eh.addWarning("Uninitialized register", eh.forParser(p1));

				if((instructions.size() >= 1 && checkRegisterWrite(instructions.get(instructions.size() - 1), ((Token.Register)p1).number)) || (instructions.size() >= 2 && checkRegisterWrite(instructions.get(instructions.size() - 2), ((Token.Register)p1).number)))
					eh.addWarning("Used old register value", eh.forParser(p1));

				instructions.add(new Instruction((Token.Mnemonic)t, p1));
				break;
			case JAL:
				if(tokens.size() - i - 1 < 1)
					throw new AssemblerException("Expected 1 parameters", eh.forParser(t));

				p1 = tokens.get(++i);

				if(!(p1 instanceof Token.Immediate) && !(p1 instanceof Token.Reference))
					throw new AssemblerException("Expected immediate or label name", eh.forParser(p1));

				if(p1 instanceof Token.Immediate)
				{
					if(((Token.Immediate)p1).value < -33554432 || ((Token.Immediate)p1).value > 33554431)
						eh.addWarning("Immediate out of signed 26 bit range", eh.forParser(p1));

					if((instructions.size() >= 1 && checkRegisterWrite(instructions.get(instructions.size() - 1), 31)) || (instructions.size() >= 2 && checkRegisterWrite(instructions.get(instructions.size() - 2), 31)))
						eh.addWarning("Link register 31 will be overwritten", eh.forParser(t));

					if(instructions.size() >= 3 && checkRegisterWrite(instructions.get(instructions.size() - 3), 31))
						eh.addWarning("Concurrent write to register 31", eh.forParser(t));

					instructions.add(new Instruction((Token.Mnemonic)t, p1));
				}
				else
				{
					Integer imm = labels.get(((Token.Reference)p1).label);

					if(imm == null)
						throw new AssemblerException("Label not defined", eh.forParser(p1));

					imm = imm - instructions.size();
					if(imm < -33554432 || imm > 33554431)
						eh.addWarning("Label out of signed 26 bit range", eh.forParser(p1));

					if((instructions.size() >= 1 && checkRegisterWrite(instructions.get(instructions.size() - 1), 31)) || (instructions.size() >= 2 && checkRegisterWrite(instructions.get(instructions.size() - 2), 31)))
						eh.addWarning("Link in register 31 will be overwritten", eh.forParser(t));

					if(instructions.size() >= 3 && checkRegisterWrite(instructions.get(instructions.size() - 3), 31))
						eh.addWarning("Concurrent write to register 31", eh.forParser(t));

					register_use[31] = true;
					instructions.add(new Instruction((Token.Mnemonic)t, new Token.Immediate(imm)));
				}
				break;
			case JALR:
				if(tokens.size() - i - 1 < 1)
					throw new AssemblerException("Expected 1 parameters", eh.forParser(t));

				p1 = tokens.get(++i);

				if(!(p1 instanceof Token.Register))
					throw new AssemblerException("Expected register", eh.forParser(p1));

				if(!register_use[((Token.Register)p1).number])
					eh.addWarning("Uninitialized register", eh.forParser(p1));

				if((instructions.size() >= 1 && checkRegisterWrite(instructions.get(instructions.size() - 1), ((Token.Register)p1).number)) || (instructions.size() >= 2 && checkRegisterWrite(instructions.get(instructions.size() - 2), ((Token.Register)p1).number)))
					eh.addWarning("Used old register value", eh.forParser(p1));

				if((instructions.size() >= 1 && checkRegisterWrite(instructions.get(instructions.size() - 1), 31)) || (instructions.size() >= 2 && checkRegisterWrite(instructions.get(instructions.size() - 2), 31)))
					eh.addWarning("Link register 31 will be overwritten", eh.forParser(t));

				if(instructions.size() >= 3 && checkRegisterWrite(instructions.get(instructions.size() - 3), 31))
					eh.addWarning("Concurrent write to register 31", eh.forParser(t));

				register_use[31] = true;
				instructions.add(new Instruction((Token.Mnemonic)t, p1));
			}
				
		}
	}

	private static final boolean checkRegisterLoad(Instruction ins, int register)
	{
		switch(ins.mnemonic.name)
		{
		case LB:
		case LBU:
		case LH:
		case LHU:
		case LW:
			if(((Token.Register)ins.parameters[0]).number == register)
				return true;
		}

		return false;
	}

	private static final boolean checkRegisterWrite(Instruction ins, int register)
	{
		if(register == 0)
			return false;

		switch(ins.mnemonic.name)
		{
		case LB:
		case LBU:
		case LH:
		case LHU:
		case LW:
		case ADD:
		case SUB:
		case MULT:
		case MULTU:
		case DIV:
		case DIVU:
		case AND:
		case OR:
		case XOR:
		case SLL:
		case SRL:
		case SRA:
		case SLT:
		case SGT:
		case SLE:
		case SGE:
		case SEQ:
		case SNE:
		case ADDI:
		case SUBI:
		case SLLI:
		case SRLI:
		case SRAI:
		case SLTI:
		case SGTI:
		case SLEI:
		case SGEI:
		case SEQI:
		case SNEI:
		case ADDUI:
		case SUBUI:
		case ANDI:
		case ORI:
		case XORI:
		case LHI:
		case FADD:
		case FSUB:
		case FMULT:
		case FDIV:
		case FSLT:
		case FSGT:
		case FSLE:
		case FSGE:
		case FSEQ:
		case FSNE:
		case FSINF:
		case FCITF:
		case FCFTI:
			if(((Token.Register)ins.parameters[0]).number == register)
				return true;
		}

		return false;
	}
}

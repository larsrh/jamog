
package gui;

import test.*;
import core.misc.setable.Setable;
import core.signal.Bit;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static core.signal.Bit.*;
import static core.misc.BitConverter.*;

/**
 *
 * @author torben
 */
public final class DLXAssembler
{
	public static class Token
	{
		public String error;
		public String warning;

		public Token()
		{
			error = null;
			warning = null;
		}

		public void setError(String error)
		{
			if(this.error == null)
				this.error = error;
		}

		public void setWarning(String warning)
		{
			if(this.warning == null)
				this.warning = warning;
		}
	}

	public static final class CommandToken extends Token
	{
		private static enum Command
		{
			LB(new Bit[] {H, L, L, L, L, L}), LBU(new Bit[] {L, H, L, L, L, L}), SB(new Bit[] {H, H, L, L, L, L}),
			LH(new Bit[] {L, L, H, L, L, L}), LHU(new Bit[] {H, L, H, L, L, L}), SH(new Bit[] {L, H, H, L, L, L}),
			LW(new Bit[] {H, H, H, L, L, L}), SW(new Bit[] {L, L, L, H, L, L}),

			ADD(new Bit[] {L, L, L, L, L, L, L, L, L, L, L}), ADDI(new Bit[] {H, H, H, H, L, L}), ADDUI(new Bit[] {L, L, L, L, H, L}),
			SUB(new Bit[] {H, L, L, L, L, L, L, L, L, L, L}), SUBI(new Bit[] {H, L, L, L, H, L}), SUBUI(new Bit[] {L, H, L, L, H, L}),
			MULT(new Bit[] {L, H, L, L, L, L, L, L, L, L, L}), MULTU(new Bit[] {H, H, L, L, L, L, L, L, L, L, L}), DIV(new Bit[] {L, L, H, L, L, L, L, L, L, L, L}), DIVU(new Bit[] {H, L, H, L, L, L, L, L, L, L, L}),
			AND(new Bit[] {L, H, H, L, L, L, L, L, L, L, L}), ANDI(new Bit[] {H, H, L, L, H, L}),
			OR(new Bit[] {H, H, H, L, L, L, L, L, L, L, L}), ORI(new Bit[] {L, L, H, L, H, L}), XOR(new Bit[] {L, L, L, H, L, L, L, L, L, L, L}), XORI(new Bit[] {H, L, H, L, H, L}),
			LHI(new Bit[] {L, H, H, L, H, L}),
			SLL(new Bit[] {H, L, L, H, L, L, L, L, L, L, L}), SRL(new Bit[] {L, H, L, H, L, L, L, L, L, L, L}), SRA(new Bit[] {H, H, L, H, L, L, L, L, L, L, L}), SLLI(new Bit[] {H, H, H, L, H, L}), SRLI(new Bit[] {L, L, L, H, H, L}), SRAI(new Bit[] {H, L, L, H, H, L}),
			SLT(new Bit[] {L, L, H, H, L, L, L, L, L, L, L}), SGT(new Bit[] {H, L, H, H, L, L, L, L, L, L, L}), SLE(new Bit[] {L, H, H, H, L, L, L, L, L, L, L}), SGE(new Bit[] {H, H, H, H, L, L, L, L, L, L, L}), SEQ(new Bit[] {L, L, L, L, H, L, L, L, L, L, L}), SNE(new Bit[] {H, L, L, L, H, L, L, L, L, L, L}),
			SLTI(new Bit[] {L, H, L, H, H, L}), SGTI(new Bit[] {H, H, L, H, H, L}), SLEI(new Bit[] {L, L, H, H, H, L}), SGEI(new Bit[] {H, L, H, H, H, L}), SEQI(new Bit[] {L, H, H, H, H, L}), SNEI(new Bit[] {H, H, H, H, H, L}),

			BEQZ(new Bit[] {H, L, L, H, L, L}), BNEZ(new Bit[] {L, H, L, H, L, L}),
			J(new Bit[] {H, H, L, H, L, L}), JR(new Bit[] {L, L, H, H, L, L}),
			JAL(new Bit[] {H, L, H, H, L, L}), JALR(new Bit[] {L, H, H, H, L, L}),

			FADD(new Bit[] {L, L, L, L, L, H, L, L, L, L, L}), FSUB(new Bit[] {H, L, L, L, L, H, L, L, L, L, L}),
			FMULT(new Bit[] {L, H, L, L, L, H, L, L, L, L, L}), FDIV(new Bit[] {H, H, L, L, L, H, L, L, L, L, L}),
			FSLT(new Bit[] {L, L, H, L, L, H, L, L, L, L, L}), FSGT(new Bit[] {H, L, H, L, L, H, L, L, L, L, L}), FSLE(new Bit[] {L, H, H, L, L, H, L, L, L, L, L}), FSGE(new Bit[] {H, H, H, L, L, H, L, L, L, L, L}), FSEQ(new Bit[] {L, L, L, H, L, H, L, L, L, L, L}), FSNE(new Bit[] {H, L, L, H, L, H, L, L, L, L, L}),
			FSINF(new Bit[] {L, H, L, H, L, H, L, L, L, L, L}), FCITF(new Bit[] {H, H, L, H, L, H, L, L, L, L, L}), FCFTI(new Bit[] {L, L, H, H, L, H, L, L, L, L, L});

			private final Bit[] opcode;

			private Command(Bit[] opcode)
			{
				this.opcode = opcode;
			}
		}

		private final Command command;

		public CommandToken(String command)
		{
			this.command = Command.valueOf(command.toUpperCase());
		}
	}

	public static final class RegisterToken extends Token
	{
		private final int register;

		public RegisterToken(String register)
		{
			this.register = Integer.parseInt(register);
		}
	}

	public static final class ImmediateToken extends Token
	{
		private final int immediate;

		public ImmediateToken(String immediate)
		{
			if(immediate.charAt(0) == '0')
			{
				if(immediate.length() >= 3 && immediate.charAt(1) == 'x')
					this.immediate = Integer.parseInt(immediate.substring(2), 16);
				else if(immediate.length() >= 3 && immediate.charAt(1) == 'b')
					this.immediate = Integer.parseInt(immediate.substring(2), 2);
				else if(immediate.length() >= 2 && immediate.charAt(1) != 'x' && immediate.charAt(1) != 'b')
					this.immediate = Integer.parseInt(immediate.substring(1), 8);
				else
					this.immediate = 0;
			}
			else
				this.immediate = Integer.parseInt(immediate);
		}

		public ImmediateToken(int immediate)
		{
			this.immediate = immediate;
		}
	}

	public static final class AddressToken extends Token
	{
		private final int register;
		private final int immediate;

		public AddressToken(String immediate, String register)
		{
			this.register = Integer.parseInt(register);

			if(immediate.charAt(0) == '0')
			{
				if(immediate.length() >= 3 && immediate.charAt(1) == 'x')
					this.immediate = Integer.parseInt(immediate.substring(2), 16);
				else if(immediate.length() >= 3 && immediate.charAt(1) == 'b')
					this.immediate = Integer.parseInt(immediate.substring(2), 2);
				else if(immediate.length() >= 2 && immediate.charAt(1) != 'x' && immediate.charAt(1) != 'b')
					this.immediate = Integer.parseInt(immediate.substring(1), 8);
				else
					this.immediate = 0;
			}
			else
				this.immediate = Integer.parseInt(immediate);

		}
	}

	public static final class NameToken extends Token
	{
		private final String name;

		public NameToken(String name)
		{
			this.name = name;
		}
	}

	public static final class LabelToken extends Token
	{
		private final String label;

		public LabelToken(String label)
		{
			this.label = label;
		}
	}

	public static final class CommentToken extends Token
	{
		private final String comment;

		public CommentToken(String comment)
		{
			this.comment = comment;
		}
	}

	public static final Token[] parse(String asm, Setable memory)
	{
		Token[] pos = new Token[asm.length()];
		for(int i = 0; i < asm.length(); ++i)
			pos[i] = null;

		for(Map.Entry<Pattern, Type> e : regex.entrySet())
		{
			Matcher m = e.getKey().matcher(asm);

			match: while(m.find())
			{
				Token t;

				switch(e.getValue())
				{
				case COMMAND:
					t = new CommandToken(asm.substring(m.start(1), m.end(1)));

					for(int i = m.start(); i < m.end(); ++i)
						if(pos[i] != null)
							continue match;
					for(int i = m.start(); i < m.end(); ++i)
						pos[i] = t;

					break;
				case REGISTER:
					t = new RegisterToken(asm.substring(m.start(1), m.end(1)));

					for(int i = m.start(); i < m.end(); ++i)
						if(pos[i] != null)
							continue match;
					for(int i = m.start(); i < m.end(); ++i)
						pos[i] = t;

					break;
				case IMMEDIATE:
					t = new ImmediateToken(asm.substring(m.start(1), m.end(1)));

					for(int i = m.start(); i < m.end(); ++i)
						if(pos[i] != null)
							continue match;
					for(int i = m.start(); i < m.end(); ++i)
						pos[i] = t;

					break;
				case ADDRESS:
					t = new AddressToken(asm.substring(m.start(1), m.end(1)), asm.substring(m.start(2), m.end(2)));

					for(int i = m.start(); i < m.end(); ++i)
						if(pos[i] != null)
							continue match;
					for(int i = m.start(); i < m.end(); ++i)
						pos[i] = t;

					break;
				case NAME:
					t = new NameToken(asm.substring(m.start(1), m.end(1)));

					for(int i = m.start(); i < m.end(); ++i)
						if(pos[i] != null)
							continue match;
					for(int i = m.start(); i < m.end(); ++i)
						pos[i] = t;

					break;
				case LABEL:
					t = new LabelToken(asm.substring(m.start(1), m.end(1)));

					for(int i = m.start(); i < m.end(); ++i)
						if(pos[i] != null)
							continue match;
					for(int i = m.start(); i < m.end(); ++i)
						pos[i] = t;

					break;
				case COMMENT:
					t = new CommentToken(asm.substring(m.start(1), m.end(1)));

					if(pos[m.start()] == null)
						for(int i = m.start(); i < m.end(); ++i)
							pos[i] = t;
				}
			}
		}

		ArrayList<Token> tokens = new ArrayList<Token>();
		Map<String, Integer> labels = new HashMap<String, Integer>();

		for(int i = 0, in = 0; i < asm.length(); ++i)
		{
			if(tokens.isEmpty() || tokens.get(tokens.size() - 1) != pos[i])
			{
				if(pos[i] == null)
				{
					char c = asm.charAt(i);
					if(c != ' ' && c != '\t' && c != '\n')
					{
						pos[i] = new Token();
						pos[i].setError("Unrecognized token");
					}
					else
						continue;
				}

				if(pos[i] instanceof LabelToken)
				{
					labels.put(((LabelToken)pos[i]).label, in);
					continue;
				}
				else if(pos[i] instanceof CommandToken)
					++in;
				else if(pos[i] instanceof CommentToken)
					continue;

				tokens.add(pos[i]);
			}
		}

		boolean[] register_use = new boolean[32];
		register_use[0] = true;
		for(int i = 1; i < 32; ++i)
			register_use[i] = false;

		ArrayList<Command> commands = new ArrayList<Command>();

		for(int i = 0; i < tokens.size(); ++i)
		{
			Token c = tokens.get(i);
			Token p1, p2, p3;

			if(c instanceof CommandToken)
			{
				switch(((CommandToken)c).command)
				{
				case LB:
				case LBU:
				case LH:
				case LHU:
				case LW:
					if(tokens.size() - i - 1 < 2)
						c.setError("Expected 2 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p2 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof RegisterToken))
						p1.setError("Expected register");
					if(p2 != null && !(p2 instanceof AddressToken))
						p2.setError("Expected address");

					if(p1 instanceof RegisterToken && p2 instanceof AddressToken)
					{
						if(!register_use[((AddressToken)p2).register])
							p2.setWarning("Uninitialized register");

						if(((AddressToken)p2).immediate < 0 || ((AddressToken)p2).immediate > 65535)
							p2.setWarning("Offset out of unsigned 16 bit range");

						if(commands.size() >= 1 && checkRegisterLoad(commands.get(commands.size() - 1), ((AddressToken)p2).register))
							p2.setWarning("Used old register value");

						register_use[((RegisterToken)p1).register] = true;
						commands.add(new Command((CommandToken)c, p1, p2));
					}
					break;
				case SB:
				case SH:
				case SW:
					if(tokens.size() - i - 1 < 2)
						c.setError("Expected 2 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p2 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof AddressToken))
						p1.setError("Expected address");
					if(p2 != null && !(p2 instanceof RegisterToken))
						p2.setError("Expected register");

					if(p1 instanceof AddressToken && p2 instanceof RegisterToken)
					{
						if(!register_use[((AddressToken)p1).register])
							p1.setWarning("Uninitialized register");
						if(!register_use[((RegisterToken)p2).register])
							p2.setWarning("Uninitialized register");

						if(((AddressToken)p1).immediate < 0 || ((AddressToken)p1).immediate > 65535)
							p2.setWarning("Offset out of unsigned 16 bit range");

						if(commands.size() >= 1)
						{
							if(checkRegisterLoad(commands.get(commands.size() - 1), ((AddressToken)p1).register))
								p1.setWarning("Used old register value");
							if(checkRegisterLoad(commands.get(commands.size() - 1), ((RegisterToken)p2).register))
								p2.setWarning("Used old register value");
						}

						commands.add(new Command((CommandToken)c, p1, p2));
					}
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
						c.setError("Expected 3 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p2 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p3 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof RegisterToken))
						p1.setError("Expected register");
					if(p2 != null && !(p2 instanceof RegisterToken))
						p2.setError("Expected register");
					if(p3 != null && !(p3 instanceof RegisterToken))
						p3.setError("Expected register");

					if(p1 instanceof RegisterToken && p2 instanceof RegisterToken && p3 instanceof RegisterToken)
					{
						if(!register_use[((RegisterToken)p2).register])
							p2.setWarning("Uninitialized register");
						if(!register_use[((RegisterToken)p3).register])
							p3.setWarning("Uninitialized register");

						if(commands.size() >= 1)
						{
							if(checkRegisterLoad(commands.get(commands.size() - 1), ((RegisterToken)p2).register))
								p2.setWarning("Used old register value");
							if(checkRegisterLoad(commands.get(commands.size() - 1), ((RegisterToken)p3).register))
								p3.setWarning("Used old register value");
						}

						register_use[((RegisterToken)p1).register] = true;
						commands.add(new Command((CommandToken)c, p1, p2, p3));
					}
					break;
				case FSINF:
				case FCITF:
				case FCFTI:
					if(tokens.size() - i - 1 < 2)
						c.setError("Expected 2 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p2 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof RegisterToken))
						p1.setError("Expected register");
					if(p2 != null && !(p2 instanceof RegisterToken))
						p2.setError("Expected register");

					if(p1 instanceof RegisterToken && p2 instanceof RegisterToken)
					{
						if(!register_use[((RegisterToken)p2).register])
							p2.setWarning("Uninitialized register");

						if(commands.size() >= 1 && checkRegisterLoad(commands.get(commands.size() - 1), ((RegisterToken)p2).register))
							p2.setWarning("Used old register value");

						register_use[((RegisterToken)p1).register] = true;
						commands.add(new Command((CommandToken)c, p1, p2));
					}
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
						c.setError("Expected 3 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p2 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p3 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof RegisterToken))
						p1.setError("Expected register");
					if(p2 != null && !(p2 instanceof RegisterToken))
						p2.setError("Expected register");
					if(p3 != null && !(p3 instanceof ImmediateToken))
						p3.setError("Expected immediate");

					if(p1 instanceof RegisterToken && p2 instanceof RegisterToken && p3 instanceof ImmediateToken)
					{
						if(!register_use[((RegisterToken)p2).register])
							p2.setWarning("Uninitialized register");

						if(((ImmediateToken)p3).immediate < -32768 || ((ImmediateToken)p3).immediate > 32767)
							p3.setWarning("Immediate out of signed 16 bit range");

						if(commands.size() >= 1 && checkRegisterLoad(commands.get(commands.size() - 1), ((RegisterToken)p2).register))
							p2.setWarning("Used old register value");

						register_use[((RegisterToken)p1).register] = true;
						commands.add(new Command((CommandToken)c, p1, p2, p3));
					}
					break;
				case ADDUI:
				case SUBUI:
				case ANDI:
				case ORI:
				case XORI:
					if(tokens.size() - i - 1 < 3)
						c.setError("Expected 3 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p2 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p3 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof RegisterToken))
						p1.setError("Expected register");
					if(p2 != null && !(p2 instanceof RegisterToken))
						p2.setError("Expected register");
					if(p3 != null && !(p3 instanceof ImmediateToken))
						p3.setError("Expected immediate");

					if(p1 instanceof RegisterToken && p2 instanceof RegisterToken && p3 instanceof ImmediateToken)
					{
						if(!register_use[((RegisterToken)p2).register])
							p2.setWarning("Uninitialized register");

						if(((ImmediateToken)p3).immediate < 0 || ((ImmediateToken)p3).immediate > 65535)
							p3.setWarning("Immediate out of unsigned 16 bit range");

						if(commands.size() >= 1 && checkRegisterLoad(commands.get(commands.size() - 1), ((RegisterToken)p2).register))
							p2.setWarning("Used old register value");

						register_use[((RegisterToken)p1).register] = true;
						commands.add(new Command((CommandToken)c, p1, p2, p3));
					}
					break;
				case LHI:
					if(tokens.size() - i - 1 < 2)
						c.setError("Expected 2 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p2 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof RegisterToken))
						p1.setError("Expected register");
					if(p2 != null && !(p2 instanceof ImmediateToken))
						p2.setError("Expected immediate");

					if(p1 instanceof RegisterToken && p2 instanceof ImmediateToken)
					{
						if(((ImmediateToken)p2).immediate < 0 || ((ImmediateToken)p2).immediate > 65535)
							p2.setWarning("Immediate out of unsigned 16 bit range");

						register_use[((RegisterToken)p1).register] = true;
						commands.add(new Command((CommandToken)c, p1, p2));
					}
					break;
				case BEQZ:
				case BNEZ:
					if(tokens.size() - i - 1 < 2)
						c.setError("Expected 2 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);
					p2 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof RegisterToken))
						p1.setError("Expected register");
					if(p2 != null && !(p2 instanceof ImmediateToken) && !(p2 instanceof NameToken))
						p2.setError("Expected immediate or label name");

					if(p1 instanceof RegisterToken && p2 instanceof ImmediateToken)
					{
						if(!register_use[((RegisterToken)p1).register])
							p1.setWarning("Uninitialized register");

						if((commands.size() >= 1 && checkRegisterWrite(commands.get(commands.size() - 1), ((RegisterToken)p1).register)) || (commands.size() >= 2 && checkRegisterWrite(commands.get(commands.size() - 2), ((RegisterToken)p1).register)))
							p1.setWarning("Used old register value");

						if(((ImmediateToken)p2).immediate < -32768 || ((ImmediateToken)p2).immediate > 32767)
							p2.setWarning("Immediate out of signed 16 bit range");

						commands.add(new Command((CommandToken)c, p1, p2));
					}
					else if(p1 instanceof RegisterToken && p2 instanceof NameToken)
					{
						Integer imm = labels.get(((NameToken)p2).name);
						if(imm != null)
						{
							if(!register_use[((RegisterToken)p1).register])
								p1.setWarning("Uninitialized register");

							if((commands.size() >= 1 && checkRegisterWrite(commands.get(commands.size() - 1), ((RegisterToken)p1).register)) || (commands.size() >= 2 && checkRegisterWrite(commands.get(commands.size() - 2), ((RegisterToken)p1).register)))
								p1.setWarning("Used old register value");

							imm = imm - commands.size();
							if(imm < -32768 || imm > 32767)
								p2.setWarning("Label out of signed 16 bit range");

							commands.add(new Command((CommandToken)c, p1, new ImmediateToken(imm)));
						}
						else
							p2.setError("Label not defined");
					}
					break;
				case J:
					if(tokens.size() - i - 1 < 1)
						c.setError("Expected 1 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof ImmediateToken) && !(p1 instanceof NameToken))
						p1.setError("Expected immediate or label name");

					if(p1 instanceof ImmediateToken)
					{
						if(((ImmediateToken)p1).immediate < -33554432 || ((ImmediateToken)p1).immediate > 33554431)
							p1.setWarning("Immediate out of signed 26 bit range");

						commands.add(new Command((CommandToken)c, p1));
					}
					else if(p1 instanceof NameToken)
					{
						Integer imm = labels.get(((NameToken)p1).name);
						if(imm != null)
						{
							imm = imm - commands.size();
							if(imm < -33554432 || imm > 33554431)
								p1.setWarning("Label out of signed 26 bit range");

							commands.add(new Command((CommandToken)c, new ImmediateToken(imm)));
						}
						else
							p1.setError("Label not defined");
					}
					break;
				case JR:
					if(tokens.size() - i - 1 < 1)
						c.setError("Expected 1 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof RegisterToken))
						p1.setError("Expected register");

					if(p1 instanceof RegisterToken)
					{
						if(!register_use[((RegisterToken)p1).register])
							p1.setWarning("Uninitialized register");

						if((commands.size() >= 1 && checkRegisterWrite(commands.get(commands.size() - 1), ((RegisterToken)p1).register)) || (commands.size() >= 2 && checkRegisterWrite(commands.get(commands.size() - 2), ((RegisterToken)p1).register)))
							p1.setWarning("Used old register value");

						commands.add(new Command((CommandToken)c, p1));
					}
					break;
				case JAL:
					if(tokens.size() - i - 1 < 1)
						c.setError("Expected 1 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof ImmediateToken) && !(p1 instanceof NameToken))
						p1.setError("Expected immediate or label name");

					if(p1 instanceof ImmediateToken)
					{
						if(((ImmediateToken)p1).immediate < -33554432 || ((ImmediateToken)p1).immediate > 33554431)
							p1.setWarning("Immediate out of signed 26 bit range");

						if((commands.size() >= 1 && checkRegisterWrite(commands.get(commands.size() - 1), 31)) || (commands.size() >= 2 && checkRegisterWrite(commands.get(commands.size() - 2), 31)))
							c.setWarning("Link register 31 will be overwritten");

						if(commands.size() >= 3 && checkRegisterWrite(commands.get(commands.size() - 3), 31))
							c.setWarning("Concurrent write to register 31");

						commands.add(new Command((CommandToken)c, p1));
					}
					else if(p1 instanceof NameToken)
					{
						Integer imm = labels.get(((NameToken)p1).name);
						if(imm != null)
						{
							imm = imm - commands.size();
							if(imm < -33554432 || imm > 33554431)
								p1.setWarning("Label out of signed 26 bit range");

							if((commands.size() >= 1 && checkRegisterWrite(commands.get(commands.size() - 1), 31)) || (commands.size() >= 2 && checkRegisterWrite(commands.get(commands.size() - 2), 31)))
								c.setWarning("Link in register 31 will be overwritten");

							if(commands.size() >= 3 && checkRegisterWrite(commands.get(commands.size() - 3), 31))
								c.setWarning("Concurrent write to register 31");

							register_use[31] = true;
							commands.add(new Command((CommandToken)c, new ImmediateToken(imm)));
						}
						else
							p1.setError("Label not defined");
					}
					break;
				case JALR:
					if(tokens.size() - i - 1 < 1)
						c.setError("Expected 1 parameters");

					p1 = i == tokens.size() - 1 ? null : tokens.get(++i);

					if(p1 != null && !(p1 instanceof RegisterToken))
						p1.setError("Expected register");

					if(p1 instanceof RegisterToken)
					{
						if(!register_use[((RegisterToken)p1).register])
							p1.setWarning("Uninitialized register");

						if((commands.size() >= 1 && checkRegisterWrite(commands.get(commands.size() - 1), ((RegisterToken)p1).register)) || (commands.size() >= 2 && checkRegisterWrite(commands.get(commands.size() - 2), ((RegisterToken)p1).register)))
							p1.setWarning("Used old register value");

						if((commands.size() >= 1 && checkRegisterWrite(commands.get(commands.size() - 1), 31)) || (commands.size() >= 2 && checkRegisterWrite(commands.get(commands.size() - 2), 31)))
							c.setWarning("Link register 31 will be overwritten");

						if(commands.size() >= 3 && checkRegisterWrite(commands.get(commands.size() - 3), 31))
							c.setWarning("Concurrent write to register 31");

						register_use[31] = true;
						commands.add(new Command((CommandToken)c, p1));
					}
				}
			}
			else
				c.setError("Expected command");
		}

		if(memory != null)
		{
			if(commands.size() * 32 > memory.getSetableCount())
				for(int i = memory.getSetableCount() / 32; i < commands.size(); ++i)
					commands.get(i).command.setError("Out of instruction memory");
			else
				for(int i = commands.size() * 32; i < memory.getSetableCount(); ++i)
					memory.setSetableBit(i, L);

			for(int i = 0; i < commands.size() && i < memory.getSetableCount() / 32; ++i)
			{
				Command c = commands.get(i);
				Bit[] rs1;
				Bit[] rs2;
				Bit[] rd;
				Bit[] imm;

				switch(c.command.command)
				{
				case LB:
				case LBU:
				case LH:
				case LHU:
				case LW:
					rs1 = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((AddressToken)c.params[1]).register));
					rd = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[0]).register));
					imm = integerToBits(Order.LITTLE_ENDIAN, 16, BigInteger.valueOf(((AddressToken)c.params[1]).immediate));
					for(int j = 0; j < 6; ++j)
						memory.setSetableBit(32 * i + j, c.command.command.opcode[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 6, rs1[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 11, rd[j]);
					for(int j = 0; j < 16; ++j)
						memory.setSetableBit(32 * i + j + 16, imm[j]);
					break;
				case SB:
				case SH:
				case SW:
					rs1 = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((AddressToken)c.params[0]).register));
					rd = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[1]).register));
					imm = integerToBits(Order.LITTLE_ENDIAN, 16, BigInteger.valueOf(((AddressToken)c.params[0]).immediate));
					for(int j = 0; j < 6; ++j)
						memory.setSetableBit(32 * i + j, c.command.command.opcode[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 6, rs1[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 11, rd[j]);
					for(int j = 0; j < 16; ++j)
						memory.setSetableBit(32 * i + j + 16, imm[j]);
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
					rs1 = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[1]).register));
					rs2 = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[2]).register));
					rd = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[0]).register));
					for(int j = 0; j < 6; ++j)
						memory.setSetableBit(32 * i + j, L);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 6, rs1[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 11, rs2[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 16, rd[j]);
					for(int j = 0; j < 11; ++j)
						memory.setSetableBit(32 * i + j + 21, c.command.command.opcode[j]);
					break;
				case FSINF:
				case FCITF:
				case FCFTI:
					rs1 = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[1]).register));
					rd = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[0]).register));
					for(int j = 0; j < 6; ++j)
						memory.setSetableBit(32 * i + j, L);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 6, rs1[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 11, L);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 16, rd[j]);
					for(int j = 0; j < 11; ++j)
						memory.setSetableBit(32 * i + j + 21, c.command.command.opcode[j]);
					break;
				case ADDI:
				case ADDUI:
				case SUBI:
				case SUBUI:
				case ANDI:
				case ORI:
				case XORI:
				case SLLI:
				case SRLI:
				case SRAI:
				case SLTI:
				case SGTI:
				case SLEI:
				case SGEI:
				case SEQI:
				case SNEI:
					rs1 = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[1]).register));
					rd = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[0]).register));
					imm = integerToBits(Order.LITTLE_ENDIAN, 16, BigInteger.valueOf(((ImmediateToken)c.params[2]).immediate));
					for(int j = 0; j < 6; ++j)
						memory.setSetableBit(32 * i + j, c.command.command.opcode[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 6, rs1[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 11, rd[j]);
					for(int j = 0; j < 16; ++j)
						memory.setSetableBit(32 * i + j + 16, imm[j]);
					break;
				case LHI:
					rd = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[0]).register));
					imm = integerToBits(Order.LITTLE_ENDIAN, 16, BigInteger.valueOf(((ImmediateToken)c.params[1]).immediate));
					for(int j = 0; j < 6; ++j)
						memory.setSetableBit(32 * i + j, c.command.command.opcode[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 6, L);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 11, rd[j]);
					for(int j = 0; j < 16; ++j)
						memory.setSetableBit(32 * i + j + 16, imm[j]);
					break;
				case BEQZ:
				case BNEZ:
					rs1 = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[0]).register));
					imm = integerToBits(Order.LITTLE_ENDIAN, 16, BigInteger.valueOf(((ImmediateToken)c.params[1]).immediate));
					for(int j = 0; j < 6; ++j)
						memory.setSetableBit(32 * i + j, c.command.command.opcode[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 6, rs1[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 11, L);
					for(int j = 0; j < 16; ++j)
						memory.setSetableBit(32 * i + j + 16, imm[j]);
					break;
				case J:
				case JAL:
					imm = integerToBits(Order.LITTLE_ENDIAN, 26, BigInteger.valueOf(((ImmediateToken)c.params[0]).immediate));
					for(int j = 0; j < 6; ++j)
						memory.setSetableBit(32 * i + j, c.command.command.opcode[j]);
					for(int j = 0; j < 26; ++j)
						memory.setSetableBit(32 * i + j + 6, imm[j]);
					break;
				case JR:
				case JALR:
					rs1 = integerToBits(Order.LITTLE_ENDIAN, 5, BigInteger.valueOf(((RegisterToken)c.params[0]).register));
					for(int j = 0; j < 6; ++j)
						memory.setSetableBit(32 * i + j, c.command.command.opcode[j]);
					for(int j = 0; j < 5; ++j)
						memory.setSetableBit(32 * i + j + 6, rs1[j]);
					for(int j = 0; j < 21; ++j)
						memory.setSetableBit(32 * i + j + 11, L);
				}
			}
		}

		return pos;
	}

	private static enum Type
	{
		COMMAND,
		REGISTER,
		IMMEDIATE,
		ADDRESS,
		NAME,
		LABEL,
		COMMENT
	}

	private static final class Command
	{
		private final CommandToken command;
		private final Token[] params;

		public Command(CommandToken command, Token... params)
		{
			this.command = command;
			this.params = params;
		}
	}

	private static final LinkedHashMap<Pattern, Type> regex;

	static
	{
		regex = new LinkedHashMap<Pattern, Type>();

		regex.put(Pattern.compile("//(.*?)$", Pattern.MULTILINE), Type.COMMENT);
		regex.put(Pattern.compile("/\\*(.*?)\\*/", Pattern.DOTALL), Type.COMMENT);
		regex.put(Pattern.compile("\\b([A-Z_][A-Z0-9_]*):", Pattern.CASE_INSENSITIVE), Type.LABEL);
		regex.put(Pattern.compile("(0x[0-9A-F]*|0b[0-1]*|0[0-7]*|[1-9][0-9]*)\\(R([1-2][0-9]|3[0-1]|[0-9])\\)", Pattern.CASE_INSENSITIVE), Type.ADDRESS);
		regex.put(Pattern.compile("\\bR([1-2][0-9]|3[0-1]|[0-9])", Pattern.CASE_INSENSITIVE), Type.REGISTER);
		regex.put(Pattern.compile("\\b(L(?:BU?|H(U|I)?)|LW|S(?:B|H|W)|(?:ADD|SUB)(?:U?I)?|(?:MULT|DIV)U?|(?:AND|OR|XOR|SLL|SRL|SRA|S(?:LT|GT|LE|GE|EQ|NE))I?|BEQZ|BNEZ|J(?:AL)?R?|F(?:ADD|SUB|MULT|DIV|S(?:LT|GT|LE|GE|EQ|NE|INF)|C(?:ITF|FTI)))\\b", Pattern.CASE_INSENSITIVE), Type.COMMAND);
		regex.put(Pattern.compile("\\b([A-Z_][A-Z0-9_]*)\\b", Pattern.CASE_INSENSITIVE), Type.NAME);
		regex.put(Pattern.compile("(0x[0-9A-F]*|0b[0-1]*|0[0-7]*|-?[1-9][0-9]*)", Pattern.CASE_INSENSITIVE), Type.IMMEDIATE);
	}

	private static final boolean checkRegisterLoad(Command com, int register)
	{
		switch(com.command.command)
		{
		case LB:
		case LBU:
		case LH:
		case LHU:
		case LW:
			if(((RegisterToken)com.params[0]).register == register)
				return true;
		}

		return false;
	}

	private static final boolean checkRegisterWrite(Command com, int register)
	{
		if(register == 0)
			return false;

		switch(com.command.command)
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
			if(((RegisterToken)com.params[0]).register == register)
				return true;
		}

		return false;
	}

	private DLXAssembler()
	{
	}
}

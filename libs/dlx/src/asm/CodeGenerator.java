
package asm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator
{
	public static int generate(Instruction i)
	{
		int opcode = 0;

		switch(i.mnemonic.name)
		{
		case LB:
		case LBU:
		case LH:
		case LHU:
		case LW:
			opcode |= opcode_map.get(i.mnemonic.name);
			opcode |= ((Token.Address)i.parameters[1]).register_number << 6;
			opcode |= ((Token.Register)i.parameters[0]).number << 11;
			opcode |= ((Token.Address)i.parameters[1]).displacement << 16;
			break;
		case SB:
		case SH:
		case SW:
			opcode |= opcode_map.get(i.mnemonic.name);
			opcode |= ((Token.Address)i.parameters[0]).register_number << 6;
			opcode |= ((Token.Register)i.parameters[1]).number << 11;
			opcode |= ((Token.Address)i.parameters[0]).displacement << 16;
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
			opcode |= ((Token.Register)i.parameters[1]).number << 6;
			opcode |= ((Token.Register)i.parameters[2]).number << 11;
			opcode |= ((Token.Register)i.parameters[0]).number << 16;
			opcode |= opcode_map.get(i.mnemonic.name) << 21;
			break;
		case FSINF:
		case FCITF:
		case FCFTI:
			opcode |= ((Token.Register)i.parameters[1]).number << 6;
			opcode |= ((Token.Register)i.parameters[0]).number << 16;
			opcode |= opcode_map.get(i.mnemonic.name) << 21;
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
			opcode |= opcode_map.get(i.mnemonic.name);
			opcode |= ((Token.Register)i.parameters[1]).number << 6;
			opcode |= ((Token.Register)i.parameters[0]).number << 11;
			opcode |= ((Token.Immediate)i.parameters[2]).value << 16;
			break;
		case LHI:
			opcode |= opcode_map.get(i.mnemonic.name);
			opcode |= ((Token.Register)i.parameters[0]).number << 11;
			opcode |= ((Token.Immediate)i.parameters[1]).value << 16;
			break;
		case BEQZ:
		case BNEZ:
			opcode |= opcode_map.get(i.mnemonic.name);
			opcode |= ((Token.Register)i.parameters[0]).number << 6;
			opcode |= ((Token.Immediate)i.parameters[1]).value << 16;
			break;
		case J:
		case JAL:
			opcode |= opcode_map.get(i.mnemonic.name);
			opcode |= ((Token.Immediate)i.parameters[0]).value << 6;
			break;
		case JR:
		case JALR:
			opcode |= opcode_map.get(i.mnemonic.name);
			opcode |= ((Token.Register)i.parameters[0]).number << 6;
		}

		return opcode;
	}

	private static final Map<Token.Mnemonic.Name, Integer> opcode_map;

	static
	{
		opcode_map = new HashMap<Token.Mnemonic.Name, Integer>();

		opcode_map.put(Token.Mnemonic.Name.LB, 001);
		opcode_map.put(Token.Mnemonic.Name.LBU, 002);
		opcode_map.put(Token.Mnemonic.Name.SB, 003);

		opcode_map.put(Token.Mnemonic.Name.LH, 004);
		opcode_map.put(Token.Mnemonic.Name.LHU, 005);
		opcode_map.put(Token.Mnemonic.Name.SH, 006);

		opcode_map.put(Token.Mnemonic.Name.LW, 007);
		opcode_map.put(Token.Mnemonic.Name.SW, 010);

		opcode_map.put(Token.Mnemonic.Name.ADD, 0x000);
		opcode_map.put(Token.Mnemonic.Name.ADDI, 017);
		opcode_map.put(Token.Mnemonic.Name.ADDUI, 020);

		opcode_map.put(Token.Mnemonic.Name.SUB, 0x001);
		opcode_map.put(Token.Mnemonic.Name.SUBI, 021);
		opcode_map.put(Token.Mnemonic.Name.SUBUI, 022);

		opcode_map.put(Token.Mnemonic.Name.MULT, 0x002);
		opcode_map.put(Token.Mnemonic.Name.MULTU, 0x003);
		opcode_map.put(Token.Mnemonic.Name.DIV, 0x004);
		opcode_map.put(Token.Mnemonic.Name.DIVU, 0x005);

		opcode_map.put(Token.Mnemonic.Name.AND, 0x006);
		opcode_map.put(Token.Mnemonic.Name.ANDI, 023);

		opcode_map.put(Token.Mnemonic.Name.OR, 0x007);
		opcode_map.put(Token.Mnemonic.Name.ORI, 024);
		opcode_map.put(Token.Mnemonic.Name.XOR, 0x008);
		opcode_map.put(Token.Mnemonic.Name.XORI, 025);

		opcode_map.put(Token.Mnemonic.Name.LHI, 026);

		opcode_map.put(Token.Mnemonic.Name.SLL, 0x009);
		opcode_map.put(Token.Mnemonic.Name.SRL, 0x00A);
		opcode_map.put(Token.Mnemonic.Name.SRA, 0x00B);
		opcode_map.put(Token.Mnemonic.Name.SLLI, 027);
		opcode_map.put(Token.Mnemonic.Name.SRLI, 030);
		opcode_map.put(Token.Mnemonic.Name.SRAI, 031);

		opcode_map.put(Token.Mnemonic.Name.SLT, 0x00C);
		opcode_map.put(Token.Mnemonic.Name.SGT, 0x00D);
		opcode_map.put(Token.Mnemonic.Name.SLE, 0x00E);
		opcode_map.put(Token.Mnemonic.Name.SGE, 0x00F);
		opcode_map.put(Token.Mnemonic.Name.SEQ, 0x010);
		opcode_map.put(Token.Mnemonic.Name.SNE, 0x011);

		opcode_map.put(Token.Mnemonic.Name.SLTI, 032);
		opcode_map.put(Token.Mnemonic.Name.SGTI, 033);
		opcode_map.put(Token.Mnemonic.Name.SLEI, 034);
		opcode_map.put(Token.Mnemonic.Name.SGEI, 035);
		opcode_map.put(Token.Mnemonic.Name.SEQI, 036);
		opcode_map.put(Token.Mnemonic.Name.SNEI, 037);

		opcode_map.put(Token.Mnemonic.Name.BEQZ, 011);
		opcode_map.put(Token.Mnemonic.Name.BNEZ, 012);

		opcode_map.put(Token.Mnemonic.Name.J, 013);
		opcode_map.put(Token.Mnemonic.Name.JR, 014);

		opcode_map.put(Token.Mnemonic.Name.JAL, 015);
		opcode_map.put(Token.Mnemonic.Name.JALR, 016);

		opcode_map.put(Token.Mnemonic.Name.FADD, 0x020);
		opcode_map.put(Token.Mnemonic.Name.FSUB, 0x021);

		opcode_map.put(Token.Mnemonic.Name.FMULT, 0x022);
		opcode_map.put(Token.Mnemonic.Name.FDIV, 0x023);

		opcode_map.put(Token.Mnemonic.Name.FSLT, 0x024);
		opcode_map.put(Token.Mnemonic.Name.FSGT, 0x025);
		opcode_map.put(Token.Mnemonic.Name.FSLE, 0x026);
		opcode_map.put(Token.Mnemonic.Name.FSGE, 0x027);
		opcode_map.put(Token.Mnemonic.Name.FSEQ, 0x028);
		opcode_map.put(Token.Mnemonic.Name.FSNE, 0x029);

		opcode_map.put(Token.Mnemonic.Name.FSINF, 0x02A);
		opcode_map.put(Token.Mnemonic.Name.FCITF, 0x02B);
		opcode_map.put(Token.Mnemonic.Name.FCFTI, 0x02C);
	}
}

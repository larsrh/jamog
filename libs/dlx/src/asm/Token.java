
package asm;

public interface Token
{
	public static final class Address implements Token
	{
		public final int displacement;
		public final int register_number;

		public Address(int displacement, int register_number)
		{
			super();
			this.displacement = displacement;
			this.register_number = register_number;
		}
	}

	public static final class Register implements Token
	{
		public final int number;

		public Register(int number)
		{
			super();
			this.number = number;
		}
	}

	public static final class Immediate implements Token
	{
		public final int value;

		public Immediate(int value)
		{
			this.value = value;
		}
	}

	public static final class Mnemonic implements Token
	{
		public static enum Name
		{
			LB, LBU, SB,
			LH, LHU, SH,
			LW, SW,

			ADD, ADDI, ADDUI,
			SUB, SUBI, SUBUI,
			MULT, MULTU, DIV, DIVU,
			AND, ANDI,
			OR, ORI, XOR, XORI,
			LHI,
			SLL, SRL, SRA, SLLI, SRLI, SRAI,
			SLT, SGT, SLE, SGE, SEQ, SNE,
			SLTI, SGTI, SLEI, SGEI, SEQI, SNEI,

			BEQZ, BNEZ,
			J, JR,
			JAL, JALR,

			FADD, FSUB,
			FMULT, FDIV,
			FSLT, FSGT, FSLE, FSGE, FSEQ, FSNE,
			FSINF, FCITF, FCFTI;
		}

		public final Name name;

		public Mnemonic(String name)
		{
			this.name = Name.valueOf(name.toUpperCase());
		}
	}

	public static final class Reference implements Token
	{
		public final String label;

		public Reference(String label)
		{
			this.label = label;
		}
	}
}

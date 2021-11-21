
package gui;


import core.misc.setable.Setable;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import javax.swing.JEditorPane;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.PlainView;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import static gui.DLXAssembler.*;

/**
 *
 * @author torben
 */
public final class AssemblerEditor extends JEditorPane
{
	private final class SyntaxEditorKit extends DefaultEditorKit implements ViewFactory
	{
		@Override public ViewFactory getViewFactory()
		{
			return this;
		}

		@Override public View create(Element e)
		{
			return new SyntaxView(e);
		}
	}

	private final class SyntaxView extends PlainView implements DocumentListener
	{
		public SyntaxView(Element e)
		{
			super(e);

			getDocument().addDocumentListener(this);
		}

		@Override protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException
		{
			Document doc = getDocument();
			char[] txt = doc.getText(p0, p1 - p0).toCharArray();

			Color c = g.getColor();
			Font f = g.getFont();

			for(int i = p0; i < p1; ++i)
			{
				if(pos[i] instanceof CommandToken)
				{
					g.setColor(command_color);
					g.setFont(f.deriveFont(Font.BOLD));
				}
				else if(pos[i] instanceof RegisterToken)
				{
					g.setColor(register_color);
					g.setFont(f);
				}
				else if(pos[i] instanceof ImmediateToken)
				{
					g.setColor(immediate_color);
					g.setFont(f);
				}
				else if(pos[i] instanceof AddressToken)
				{
					g.setColor(address_color);
					g.setFont(f);
				}
				else if(pos[i] instanceof NameToken)
				{
					g.setColor(name_color);
					g.setFont(f);
				}
				else if(pos[i] instanceof LabelToken)
				{
					g.setColor(label_color);
					g.setFont(f.deriveFont(Font.BOLD));
				}
				else if(pos[i] instanceof CommentToken)
				{
					g.setColor(comment_color);
					g.setFont(f);
				}
				else
				{
					g.setColor(error_color);
					g.setFont(f.deriveFont(Font.ITALIC));
				}

				g.drawChars(txt, i - p0, 1, x, y);
				int nx = x + g.getFontMetrics().charWidth(txt[i - p0]);
				
				if(pos[i] != null)
				{
					if(pos[i].error != null)
					{
						g.setColor(error_color);
						g.drawLine(x, y + 1, nx, y + 1);
					}
					else if(pos[i].warning != null)
					{
						g.setColor(warning_color);
						g.drawLine(x, y + 1, nx, y + 1);
					}
				}

				x = nx;
			}

			g.setFont(f);
			g.setColor(c);

			return x;
		}

		@Override protected void updateDamage(DocumentEvent changes, Shape a, ViewFactory f)
		{
			getContainer().repaint();
		}

		@Override public void insertUpdate(DocumentEvent e)
		{
			Document d = getDocument();
			try{ pos = parse(d.getText(0, d.getLength()), instruction_memory); } catch(BadLocationException ex) {}
		}

		@Override public void removeUpdate(DocumentEvent e)
		{
			Document d = getDocument();
			try{ pos = parse(d.getText(0, d.getLength()), instruction_memory); } catch(BadLocationException ex) {}
		}

		@Override public void changedUpdate(DocumentEvent e)
		{
			Document d = getDocument();
			try{ pos = parse(d.getText(0, d.getLength()), instruction_memory); } catch(BadLocationException ex) {}
		}
	}

	private static final Color command_color = new Color(0, 0, 230);
	private static final Color register_color = new Color(0, 153, 0);
	private static final Color immediate_color = new Color(153, 51, 204);
	private static final Color address_color = new Color(46, 146, 199);
	private static final Color name_color = new Color(206, 123, 0);
	private static final Color label_color = new Color(206, 123, 0);
	private static final Color comment_color = new Color(150, 150, 150);
	private static final Color error_color = new Color(255, 0, 0);
	private static final Color warning_color = new Color(192, 192, 0);

	private final Setable instruction_memory;
	private Token[] pos;

	public AssemblerEditor(Setable instruction_memory)
	{
		this.instruction_memory = instruction_memory;

		pos = new Token[0];
		instruction_memory = null;

		setEditorKit(new SyntaxEditorKit());
		ToolTipManager.sharedInstance().registerComponent(this);
		setFont(Font.decode(Font.MONOSPACED));
	}

	@Override public String getToolTipText(MouseEvent event)
	{
		int p = getUI().viewToModel(this, event.getPoint());

		if(p >= 0 && p < pos.length && pos[p] != null)
		{
			if(pos[p].error != null)
				return pos[p].error;
			else if(pos[p].warning != null)
				return pos[p].warning;
		}

		return null;
	}
}

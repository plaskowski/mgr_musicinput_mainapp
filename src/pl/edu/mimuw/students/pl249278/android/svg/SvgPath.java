package pl.edu.mimuw.students.pl249278.android.svg;

public class SvgPath extends SvgObject {
	
	SvgPath(String commands, float[] args) {
		this.commands = commands;
		this.args = args;
	}
	//TODO remove public modifier and add some way to access and iterate over this
	public String commands;
	public float[] args;
}

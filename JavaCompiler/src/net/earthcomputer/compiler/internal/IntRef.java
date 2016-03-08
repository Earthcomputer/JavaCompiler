package net.earthcomputer.compiler.internal;

public class IntRef {

	private int val;

	public IntRef(int val) {
		this.val = val;
	}

	public int set(int val) {
		this.val = val;
		return val;
	}

	public int get() {
		return val;
	}
	
	public int inc() {
		return val++;
	}
	
	public int binc() {
		return ++val;
	}
	
	public int dec() {
		return val--;
	}
	
	public int bdec() {
		return --val;
	}
	
	public int add(int val) {
		return this.val += val;
	}
	
	public int sub(int val) {
		return this.val -= val;
	}
	
	public int mul(int val) {
		return this.val *= val;
	}
	
	public int div(int val) {
		return this.val /= val;
	}
	
	public int rem(int val) {
		return this.val %= val;
	}
	
	public int and(int val) {
		return this.val &= val;
	}
	
	public int or(int val) {
		return this.val |= val;
	}
	
	public int xor(int val) {
		return this.val ^= val;
	}

}

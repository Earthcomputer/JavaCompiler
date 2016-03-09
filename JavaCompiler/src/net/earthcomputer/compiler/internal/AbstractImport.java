package net.earthcomputer.compiler.internal;

public abstract class AbstractImport {

	private final EnumImportType type;
	private final String theImport;

	public AbstractImport(EnumImportType type, String theImport) {
		this.type = type;
		this.theImport = theImport;
	}

	public EnumImportType getType() {
		return type;
	}

	public String getImport() {
		return theImport;
	}

	public boolean equals(Object other) {
		if (other == this)
			return true;
		else if (!(other instanceof AbstractImport))
			return false;
		AbstractImport otherImport = (AbstractImport) other;
		return type == otherImport.type && theImport.equals(otherImport.theImport);
	}

	public int hashCode() {
		return EnumImportType.values().length * getImport().hashCode() + getType().ordinal();
	}

	public static class StandardImport extends AbstractImport {

		public StandardImport(String theImport) {
			super(EnumImportType.STANDARD, theImport);
		}

		@Override
		public String toString() {
			return "import " + getImport();
		}
	}

	public static class StandardWildcardImport extends AbstractImport {

		public StandardWildcardImport(String thePackage) {
			super(EnumImportType.STANDARD_WILDCARD, thePackage);
		}

		@Override
		public String toString() {
			return "import " + getImport() + ".*";
		}
	}

	public static class StaticImport extends AbstractImport {

		public StaticImport(String theImport) {
			super(EnumImportType.STATIC, theImport);
		}

		@Override
		public String toString() {
			return "import static " + getImport();
		}
	}

	public static class StaticWildcardImport extends AbstractImport {

		public StaticWildcardImport(String thePackage) {
			super(EnumImportType.STATIC_WILDCARD, thePackage);
		}

		@Override
		public String toString() {
			return "import static " + getImport() + ".*";
		}
	}

	public static enum EnumImportType {
		STANDARD(false, false), STANDARD_WILDCARD(false, true), STATIC(true, false), STATIC_WILDCARD(true, true);

		private final boolean isStatic;
		private final boolean isWildcard;

		private EnumImportType(boolean isStatic, boolean isWildcard) {
			this.isStatic = isStatic;
			this.isWildcard = isWildcard;
		}

		public boolean isStatic() {
			return isStatic;
		}

		public boolean isWildcard() {
			return isWildcard;
		}
	}

}

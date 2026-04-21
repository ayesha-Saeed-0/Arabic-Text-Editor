package dal;

public class MySqlFactory implements IDALFactory{

	public MySqlFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Iimport createDucumentDAO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPage createPageDAO() {
		// TODO Auto-generated method stub
		return new PageDAO();
	}

	@Override
	public ITransliterate createTransliterateDAO(DatabaseConnection connection) {
		// TODO Auto-generated method stub
		return new TransliterateDAO(connection);
	}


	@Override
	public ILemma createLemmaDAO() {
		// TODO Auto-generated method stub
		return new LemmaDAO();
	}

}

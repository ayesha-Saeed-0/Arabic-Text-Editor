package dal;

public interface IDALFactory {
 Iimport createDucumentDAO();
 IPage createPageDAO();
 ITransliterate createTransliterateDAO(DatabaseConnection connection);
 ILemma createLemmaDAO();
}

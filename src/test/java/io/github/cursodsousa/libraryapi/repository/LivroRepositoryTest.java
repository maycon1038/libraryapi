package io.github.cursodsousa.libraryapi.repository;

import io.github.cursodsousa.libraryapi.model.Autor;
import io.github.cursodsousa.libraryapi.model.GeneroLivro;
import io.github.cursodsousa.libraryapi.model.Livro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
class LivroRepositoryTest {

    @Autowired
    LivroRepository repository;

    @Autowired
    AutorRepository autorRepository;

    @BeforeEach
    void limparDados() {
        repository.deleteAll();
        autorRepository.deleteAll();
    }

    private Autor criarAutor(String nome, String nacionalidade) {
        Autor autor = new Autor();
        autor.setNome(nome);
        autor.setNacionalidade(nacionalidade);
        autor.setDataNascimento(LocalDate.of(1980, 1, 10));
        return autorRepository.save(autor);
    }

    private Livro criarLivro(String isbn, String titulo, GeneroLivro genero, BigDecimal preco, Autor autor) {
        Livro livro = new Livro();
        livro.setIsbn(isbn);
        livro.setPreco(preco);
        livro.setGenero(genero);
        livro.setTitulo(titulo);
        livro.setDataPublicacao(LocalDate.of(1980, 1, 2));
        livro.setAutor(autor);
        return repository.save(livro);
    }

    @Test
    void salvarTest(){
        Autor autor = criarAutor("Autor do Teste", "Brasileira");
        Livro livro = new Livro();
        livro.setIsbn("90887-84874");
        livro.setPreco(BigDecimal.valueOf(100));
        livro.setGenero(GeneroLivro.CIENCIA);
        livro.setTitulo("Ciencias");
        livro.setDataPublicacao(LocalDate.of(1980, 1, 2));
        livro.setAutor(autor);
        repository.save(livro);
    }

    @Test
    void salvarAutorELivroTest(){
        Autor autor = criarAutor("José", "Brasileira");
        Livro livro = new Livro();
        livro.setIsbn("90887-84875");
        livro.setPreco(BigDecimal.valueOf(100));
        livro.setGenero(GeneroLivro.FICCAO);
        livro.setTitulo("Terceiro Livro");
        livro.setDataPublicacao(LocalDate.of(1980, 1, 2));
        livro.setAutor(autor);
        repository.save(livro);
    }

    @Test
    void salvarCascadeTest(){
        Autor autor = criarAutor("João", "Brasileira");
        Livro livro = new Livro();
        livro.setIsbn("90887-84876");
        livro.setPreco(BigDecimal.valueOf(100));
        livro.setGenero(GeneroLivro.FICCAO);
        livro.setTitulo("Outro Livro");
        livro.setDataPublicacao(LocalDate.of(1980, 1, 2));
        livro.setAutor(autor);
        repository.save(livro);
    }

    @Test
    void atualizarAutorDoLivro(){
        Autor autorAntigo = criarAutor("Maria", "Brasileira");
        Autor autorNovo = criarAutor("Ana", "Americana");
        Livro livro = criarLivro("11111-22222", "Livro para Atualizar", GeneroLivro.MISTERIO, BigDecimal.valueOf(80.00), autorAntigo);

        livro.setAutor(autorNovo);
        repository.save(livro);
    }

    @Test
    void deletar(){
        Autor autor = criarAutor("Para deletar", "Brasileira");
        Livro livro = criarLivro("33333-44444", "Livro a apagar", GeneroLivro.FICCAO, BigDecimal.valueOf(50), autor);
        repository.deleteById(livro.getId());
    }

    @Test
    void deletarCascade(){
        Autor autor = criarAutor("Autor de Cascade", "Brasileira");
        Livro livro = criarLivro("33333-44445", "Livro a apagar 2", GeneroLivro.FICCAO, BigDecimal.valueOf(60), autor);
        repository.deleteById(livro.getId());
    }

    @Test
    void buscarLivroTest(){
        Autor autor = criarAutor("Busca", "Brasileira");
        Livro livro = criarLivro("99888-00001", "Livro Encontrado", GeneroLivro.CIENCIA, BigDecimal.valueOf(110), autor);
        Livro salvo = repository.findById(livro.getId()).orElseThrow();
        System.out.println("Livro:" + salvo.getTitulo());
    }

    @Test
    void pesquisaPorTituloTest(){
        Autor autor = criarAutor("Pesquisa", "Brasileira");
        criarLivro("20847-84874", "O roubo da casa assombrada", GeneroLivro.MISTERIO, BigDecimal.valueOf(204.00), autor);
        List<Livro> lista = repository.findByTitulo("O roubo da casa assombrada");
        lista.forEach(System.out::println);
    }

    @Test
    void pesquisaPorISBNTest(){
        Autor autor = criarAutor("ISBN", "Brasileira");
        criarLivro("20847-84874", "Livro por ISBN", GeneroLivro.MISTERIO, BigDecimal.valueOf(204.00), autor);
        Optional<Livro> livro = repository.findByIsbn("20847-84874");
        livro.ifPresent(System.out::println);
    }

    @Test
    void pesquisaPorTituloEPrecoTest(){
        Autor autor = criarAutor("Titulo e Preco", "Brasileira");
        criarLivro("20847-84875", "O roubo da casa assombrada", GeneroLivro.MISTERIO, BigDecimal.valueOf(204.00), autor);
        var preco = BigDecimal.valueOf(204.00);
        var tituloPesquisa = "O roubo da casa assombrada";

        List<Livro> lista = repository.findByTituloAndPreco(tituloPesquisa, preco);
        lista.forEach(System.out::println);
    }

    @Test
    void listarLivrosComQueryJPQL(){
        Autor autor = criarAutor("JPQL", "Brasileira");
        criarLivro("33333-11111", "Zeta", GeneroLivro.FICCAO, BigDecimal.valueOf(50), autor);
        criarLivro("33333-11112", "Alpha", GeneroLivro.CIENCIA, BigDecimal.valueOf(100), autor);
        var resultado = repository.listarTodosOrdenadoPorTituloAndPreco();
        resultado.forEach(System.out::println);
    }

    @Test
    void listarAutoresDosLivros(){
        Autor autor1 = criarAutor("Autor 1", "Brasileira");
        Autor autor2 = criarAutor("Autor 2", "Americana");
        criarLivro("22222-11111", "Livro A", GeneroLivro.FICCAO, BigDecimal.valueOf(60), autor1);
        criarLivro("22222-11112", "Livro B", GeneroLivro.MISTERIO, BigDecimal.valueOf(70), autor2);
        var resultado = repository.listarAutoresDosLivros();
        resultado.forEach(System.out::println);
    }

    @Test
    void listarTitulosNaoRepetidosDosLivros(){
        Autor autor = criarAutor("Titulos", "Brasileira");
        criarLivro("44444-00001", "Titulo Repetido", GeneroLivro.FICCAO, BigDecimal.valueOf(20), autor);
        criarLivro("44444-00002", "Titulo Repetido", GeneroLivro.FICCAO, BigDecimal.valueOf(30), autor);
        var resultado = repository.listarNomesDiferentesLivros();
        resultado.forEach(System.out::println);
    }

    @Test
    void listarGenerosDeLivrosAutoresBrasileiros(){
        Autor autorBrasileiro = criarAutor("Brasileiro", "Brasileira");
        criarLivro("55555-00001", "Livro Brasileiro 1", GeneroLivro.MISTERIO, BigDecimal.valueOf(10), autorBrasileiro);
        criarLivro("55555-00002", "Livro Brasileiro 2", GeneroLivro.FICCAO, BigDecimal.valueOf(20), autorBrasileiro);
        var resultado = repository.listarGenerosAutoresBrasileiros();
        resultado.forEach(System.out::println);
    }

    @Test
    void listarPorGeneroQueryParamTest(){
        Autor autor = criarAutor("Genero Query", "Brasileira");
        criarLivro("66666-00001", "Livro MISTERIO", GeneroLivro.MISTERIO, BigDecimal.valueOf(20), autor);
        var resultado = repository.findByGenero(GeneroLivro.MISTERIO, "preco");
        resultado.forEach(System.out::println);
    }

    @Test
    void listarPorGeneroPositionalParamTest(){
        Autor autor = criarAutor("Genero Positional", "Brasileira");
        criarLivro("66666-00002", "Livro MISTERIO 2", GeneroLivro.MISTERIO, BigDecimal.valueOf(25), autor);
        var resultado = repository.findByGeneroPositionalParameters("preco", GeneroLivro.MISTERIO);
        resultado.forEach(System.out::println);
    }

    @Test
    void deletePorGeneroTest(){
        Autor autor = criarAutor("Genero Delete", "Brasileira");
        criarLivro("77777-00001", "Livro CIENCIA", GeneroLivro.CIENCIA, BigDecimal.valueOf(35), autor);
        repository.deleteByGenero(GeneroLivro.CIENCIA);
    }

    @Test
    void updateDataPublicacaoTest(){
        Autor autor = criarAutor("Atualiza Data", "Brasileira");
        criarLivro("88888-00001", "Livro Atualizado", GeneroLivro.CIENCIA, BigDecimal.valueOf(40), autor);
        repository.updateDataPublicacao(LocalDate.of(2000,1,1));
    }
}
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SpringBootTest
public class AutorRepositoryTest {

    @Autowired
    AutorRepository repository;

    @Autowired
    LivroRepository livroRepository;

    @BeforeEach
    void limparDados() {
        livroRepository.deleteAll();
        repository.deleteAll();
    }

    private Autor criarAutor(String nome, String nacionalidade, LocalDate nascimento) {
        Autor autor = new Autor();
        autor.setNome(nome);
        autor.setNacionalidade(nacionalidade);
        autor.setDataNascimento(nascimento);
        return autor;
    }

    @Test
    public void salvarTest(){
        Autor autor = criarAutor("José", "Brasileira", LocalDate.of(1951, 1, 31));
        var autorSalvo = repository.save(autor);
        System.out.println("Autor Salvo: " + autorSalvo);
    }

    @Test
    public void atualizarTest(){
        Autor autor = repository.save(criarAutor("Maria", "Brasileira", LocalDate.of(1970, 2, 10)));

        Optional<Autor> possivelAutor = repository.findById(autor.getId());

        if(possivelAutor.isPresent()){
            Autor autorEncontrado = possivelAutor.get();
            autorEncontrado.setDataNascimento(LocalDate.of(1960, 1, 30));
            repository.save(autorEncontrado);
        }
    }

    @Test
    public void listarTest(){
        repository.save(criarAutor("João", "Brasileira", LocalDate.of(1980, 5, 5)));
        repository.save(criarAutor("Ana", "Americana", LocalDate.of(1988, 7, 12)));

        List<Autor> lista = repository.findAll();
        lista.forEach(System.out::println);
    }

    @Test
    public void countTest(){
        repository.save(criarAutor("Pedro", "Brasileira", LocalDate.of(1990, 3, 5)));
        repository.save(criarAutor("Luiza", "Portuguesa", LocalDate.of(1991, 7, 20)));
        System.out.println("Contagem de autores: " + repository.count());
    }

    @Test
    public void deletePorIdTest(){
        Autor autor = repository.save(criarAutor("Excluir por Id", "Brasileira", LocalDate.of(1982, 5, 8)));
        repository.deleteById(autor.getId());
    }

    @Test
    public void deleteTest(){
        Autor autor = repository.save(criarAutor("Maria", "Brasileira", LocalDate.of(1975, 9, 15)));
        repository.delete(autor);
    }

    @Test
    void salvarAutorComLivrosTest(){
        Autor autor = criarAutor("Antonio", "Americana", LocalDate.of(1970, 8, 5));
        autor = repository.save(autor);

        Livro livro = new Livro();
        livro.setIsbn("20847-84874");
        livro.setPreco(BigDecimal.valueOf(204));
        livro.setGenero(GeneroLivro.MISTERIO);
        livro.setTitulo("O roubo da casa assombrada");
        livro.setDataPublicacao(LocalDate.of(1999, 1, 2));
        livro.setAutor(autor);
        livroRepository.save(livro);

        Livro livro2 = new Livro();
        livro2.setIsbn("99999-84874");
        livro2.setPreco(BigDecimal.valueOf(650));
        livro2.setGenero(GeneroLivro.MISTERIO);
        livro2.setTitulo("O roubo da casa assombrada");
        livro2.setDataPublicacao(LocalDate.of(2000, 1, 2));
        livro2.setAutor(autor);
        livroRepository.save(livro2);
    }

    @Test
    void listarLivrosAutor(){
        Autor autor = repository.save(criarAutor("Autor com Livros", "Brasileira", LocalDate.of(1981, 3, 20)));

        Livro livro = new Livro();
        livro.setIsbn("11111-00001");
        livro.setPreco(BigDecimal.valueOf(120));
        livro.setGenero(GeneroLivro.FICCAO);
        livro.setTitulo("Livro do autor");
        livro.setDataPublicacao(LocalDate.of(2020, 1, 2));
        livro.setAutor(autor);
        livroRepository.save(livro);

        List<Livro> livrosLista = livroRepository.findByAutor(autor);
        autor.setLivros(livrosLista);
        autor.getLivros().forEach(System.out::println);
    }

}

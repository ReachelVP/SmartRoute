package cr.ac.una.smartroute.presentation.admin;

import cr.ac.una.smartroute.business.CategoriaAdminService;
import cr.ac.una.smartroute.business.exception.CategoriaInexistenteException;
import cr.ac.una.smartroute.data.entity.Categoria;
import cr.ac.una.smartroute.presentation.admin.dto.CategoriaFormulario;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Módulo administrativo Thymeleaf (Laboratorio 6), caso de uso 2 de 2: catálogo de categorías.
 * Mismo patrón que {@link LugarAdminController} (listado paginado, formulario validado,
 * Post/Redirect/Get, CSRF) sobre una entidad más simple.
 */
@Controller
@RequestMapping("/admin/categorias")
public class CategoriaAdminController {

    private final CategoriaAdminService categoriaAdminService;

    public CategoriaAdminController(CategoriaAdminService categoriaAdminService) {
        this.categoriaAdminService = categoriaAdminService;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Categoria> pagina =
                categoriaAdminService.listarPaginado(PageRequest.of(page, 10, Sort.by("nombre").ascending()));
        model.addAttribute("pagina", pagina);
        return "admin/categorias/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("categoriaFormulario", new CategoriaFormulario());
        model.addAttribute("esNuevo", true);
        return "admin/categorias/formulario";
    }

    @PostMapping("/nuevo")
    public String crear(
            @Valid @ModelAttribute("categoriaFormulario") CategoriaFormulario formulario,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("esNuevo", true);
            return "admin/categorias/formulario";
        }

        Categoria creada = categoriaAdminService.crear(formulario.getNombre(), formulario.getDescripcion());
        redirectAttributes.addFlashAttribute("mensaje", "Categoría \"" + creada.getNombre() + "\" creada.");
        return "redirect:/admin/categorias";
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Categoria categoria = categoriaAdminService.obtener(id);
            model.addAttribute("categoriaFormulario", CategoriaFormulario.desde(categoria));
            model.addAttribute("esNuevo", false);
            model.addAttribute("id", id);
            return "admin/categorias/formulario";
        } catch (CategoriaInexistenteException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/categorias";
        }
    }

    @PostMapping("/{id}/editar")
    public String actualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoriaFormulario") CategoriaFormulario formulario,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("esNuevo", false);
            model.addAttribute("id", id);
            return "admin/categorias/formulario";
        }

        try {
            Categoria actualizada = categoriaAdminService.actualizar(id, formulario.getNombre(), formulario.getDescripcion());
            redirectAttributes.addFlashAttribute("mensaje", "Categoría \"" + actualizada.getNombre() + "\" actualizada.");
        } catch (CategoriaInexistenteException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categorias";
    }
}

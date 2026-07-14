import { Component, OnInit } from '@angular/core';
import { CategoryService, Category } from '../../services/category.service';

interface CategoryDisplay {
  id: string;
  name: string;
  description: string;
  courseCount: number;
}

@Component({
  selector: 'app-home',
  standalone: false,
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {

  categories: CategoryDisplay[] = [];
  loading = false;

  constructor(private categoryService: CategoryService) { }

  ngOnInit(): void {
    this.loadCategories();
  }

  private loadCategories(): void {
    this.loading = true;
    this.categoryService.getAllCategories().subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.categories = response.data.map(category => ({
            id: category.id,
            name: category.name,
            description: category.description || `Explore courses in ${category.name}`,
            courseCount: 0 // You can fetch actual count from backend if needed
          }));
        }
      },
      error: (error) => {
        this.loading = false;
        console.error('Error loading categories:', error);
      }
    });
  }

}

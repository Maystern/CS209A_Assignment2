#include <stdio.h>
#include <ctype.h>
char str[105][1005];
int count_specific_char(char *str, int len, char ch) {
    ch = tolower(ch);
    int cnt = 0;
    for (int i = 0; i < len; i++) {
        if (tolower(str[i]) == ch) {
            cnt++;
        }
    }
    return cnt;
}
int main() {
    int n;
    printf("Enter the number of strings: ");
    scanf("%d", &n);
    char ch = 1;
    printf("Enter the specific character: ");
    while (islower(ch) == 0 && isupper(ch) == 0) scanf("%c", &ch);
    getchar();
    int max_cnt = 0, max_idx = 0;
    for (int i = 0; i < n; i++) {
        printf("Enter the string %d: ", i + 1);

        fgets(str[i], sizeof(str[i]), stdin);
        int cnt = count_specific_char(str[i], sizeof(str[i]), ch);
        if (cnt > max_cnt) {
            max_cnt = cnt;
            max_idx = i;
        }
    }
    if (max_cnt == 0) {
        printf("No specific character\n");
    } else {
        printf("The string with the most specific character is: %s", str[max_idx]);
        printf("Specific counts: %d\n", max_cnt);
    }
    return 0;
}